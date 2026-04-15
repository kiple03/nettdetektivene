package org.ntnu.backend.user.service;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.exception.UserAlreadyExistsException;
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

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final RefreshTokenRepository refreshTokenRepository;
  private final EnrollmentRepository enrollmentRepository;

  @Value("${jwt.refresh-expiration-ms}")
  private long refreshExpiration;

  @Transactional
  public LoginResponse register(@Valid CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("email", request.getEmail());
    }
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new UserAlreadyExistsException("username", request.getUsername());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    User user = userMapper.toUser(request, encodedPassword);
    user = userRepository.save(user);

    return buildLoginResponse(user);
  }

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

  public LoginResponse login(@Valid LoginRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    // Reject cross-portal logins: a STUDENT must not authenticate on the teacher
    // portal and vice-versa. Use a generic 403 so the error leaks no extra info.
    if (user.getRole() != request.getPortalRole()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "This account does not have access to the requested portal"
      );
    }

    return buildLoginResponse(user);
  }

  @Transactional
  public void logout(String token) {
    refreshTokenRepository.deleteByToken(token);
  }

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
