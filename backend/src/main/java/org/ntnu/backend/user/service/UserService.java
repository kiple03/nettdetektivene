package org.ntnu.backend.user.service;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.avatar.service.UserAvatarService;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.exception.UserAlreadyExistsException;
import org.ntnu.backend.game.service.ProgressService;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.security.RefreshTokenModel;
import org.ntnu.backend.security.RefreshTokenRepository;
import org.ntnu.backend.security.UserPrincipal;
import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.mapper.UserMapper;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service responsible for user authentication and account lifecycle management.
 *
 * <p>Handles registration, login, token refresh, and logout operations. On successful
 * authentication, issues a short-lived JWT access token together with a persisted refresh token
 * that can be used to obtain a new access token without re-entering credentials.
 *
 * <p>Portal-role enforcement is applied at login: a {@code STUDENT} account cannot authenticate
 * on the teacher portal and vice versa, preventing cross-portal access without leaking role
 * information through error messages.
 */

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final RefreshTokenRepository refreshTokenRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final UserAvatarService userAvatarService;
  private final ProgressService progressService;
  private final MedalionService medalionService;

  /** Lifetime of a refresh token in milliseconds, injected from {@code jwt.refresh-expiration-ms}. */
  @Value("${jwt.refresh-expiration-ms}")
  private long refreshExpiration;

  /**
   * Registers a new user account and returns an authenticated session.
   *
   * <p>Validates that the requested e-mail address is not already taken, encodes the
   * plain-text password, persists the new {@link User}, assigns starter avatar items,
   * and delegates to {@link #buildLoginResponse(User)} to produce tokens.
   *
   * @param request the validated registration payload containing name, e-mail, password,
   *                and desired role
   * @return a {@link LoginResponse} containing a fresh JWT access token and refresh token
   * @throws UserAlreadyExistsException if the e-mail address is already registered
   */
  @Transactional
  public LoginResponse register(@Valid CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("email", request.getEmail());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    User user = userMapper.toUser(request, encodedPassword);
    user = userRepository.save(user);

    if (user.getRole() == RoleEnum.STUDENT) {
      progressService.initProgressForStudent(user);
    }

    userAvatarService.starterItems(user);
    
    medalionService.awardInternalUsage(user.getId(), "Rekruttert detektiv", "Lagde bruker");    

    return buildLoginResponse(user);
  }

  /**
   * Issues a new session in exchange for a valid, unexpired refresh token.
   *
   * <p>The supplied refresh token is looked up in the database. If found and still
   * valid, it is immediately deleted (one-time use) and a brand-new access/refresh
   * token pair is generated for the associated user.
   *
   * @param token the refresh token string previously issued by this service
   * @return a {@link LoginResponse} containing a new JWT access token and refresh token
   * @throws BadCredentialsException if the token does not exist or has expired
   */
  @Transactional
  public LoginResponse refresh(String token) {
    RefreshTokenModel refreshToken = refreshTokenRepository.findByToken(token)
        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.delete(refreshToken);
      throw new BadCredentialsException("Refresh token expired");
    }
    refreshTokenRepository.delete(refreshToken);

    return buildLoginResponse(refreshToken.getUser());
  }

  /**
   * Authenticates a user against the requested portal and returns a session.
   *
   * <p>Verifies the e-mail/password combination and enforces portal-role alignment:
   * the role stored on the account must match the {@code portalRole} field in the
   * request. A generic {@code 403} is returned on mismatch so that no extra
   * information about the account is leaked to the caller.
   *
   * @param request the validated login payload containing e-mail, password, and portal role
   * @return a {@link LoginResponse} containing a fresh JWT access token and refresh token
   * @throws BadCredentialsException   if the e-mail is not found or the password does not match
   * @throws ResponseStatusException   with {@code 403 Forbidden} if the account role does not
   *                                   match the requested portal role
   */
  public LoginResponse login(@Valid LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    if (user.getRole() != request.getPortalRole()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "This account does not have access to the requested portal"
      );
    }

    return buildLoginResponse(user);
  }

  /**
   * Invalidates the given refresh token, effectively logging the user out.
   *
   * <p>If the token is not found the operation is silently ignored, making this
   * method safe to call even when the client is already logged out.
   *
   * @param token the refresh token string to revoke
   */
  @Transactional
  public void logout(String token) {
    refreshTokenRepository.deleteByToken(token);
  }

  /**
   * Constructs a {@link LoginResponse} for the given user.
   *
   * <p>Performs the following steps:
   * <ol>
   *   <li>Wraps the user in a {@link UserPrincipal} for Spring Security compatibility.</li>
   *   <li>For {@code STUDENT} accounts, resolves the current active classroom enrollment
   *       (where {@code leftAt} is {@code null}) to embed the classroom ID in the JWT.</li>
   *   <li>Generates a signed JWT access token via {@link JwtService}.</li>
   *   <li>Creates and persists a new {@link RefreshTokenModel} with a random UUID token
   *       and an expiry of {@code now + refreshExpiration} milliseconds.</li>
   *   <li>Maps both tokens to a {@link LoginResponse} via {@link UserMapper}.</li>
   * </ol>
   *
   * @param user the authenticated {@link User} for whom tokens should be generated
   * @return a {@link LoginResponse} containing the signed access token and the refresh token string
   */
  private LoginResponse buildLoginResponse(User user) {
    UserPrincipal principal = new UserPrincipal(user);

    UUID classroomId = null;
    if (user.getRole() == RoleEnum.STUDENT) {
      classroomId = enrollmentRepository.findByUserAndLeftAtIsNull(user)
          .map(enrollment -> enrollment.getClassroom().getId())
          .orElse(null);
    }
      String accessToken = jwtService.generateToken(principal, classroomId);

      RefreshTokenModel refreshToken = RefreshTokenModel.builder()
          .token(UUID.randomUUID().toString())
          .user(user)
          .expiresAt(Instant.now().plusMillis(refreshExpiration))
          .build();
      refreshTokenRepository.save(refreshToken);

      return userMapper.toLoginResponse(accessToken, refreshToken.getToken());

  }
}
