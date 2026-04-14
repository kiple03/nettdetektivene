package org.ntnu.backend.user.service;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.exception.UserAlreadyExistsException;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.security.RefreshTokenModel;
import org.ntnu.backend.security.RefreshTokenRepository;
import org.ntnu.backend.security.UserPrincipal;
import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.mapper.UserMapper;
import org.ntnu.backend.user.model.ClassroomModel;
import org.ntnu.backend.user.model.UserModel;
import org.ntnu.backend.user.model.enums.RoleEnum;

import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${jwt.refresh-expiration-ms}")
  private long refreshExpiration;


  public void joinClassroom(UserModel student, ClassroomModel classroom) {
    if (student.getRole() != RoleEnum.STUDENT) {
      throw new IllegalArgumentException("Must be student to join classroom");
    }
    if (!student.getClassrooms().isEmpty()) {
      throw new IllegalStateException("Student already in a classroom");
    }
    student.getClassrooms().add(classroom);
  }

  public LoginResponse register(@Valid CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("email", request.getEmail());
    }
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new UserAlreadyExistsException("username", request.getUsername());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    UserModel user = userMapper.toUserModel(request, encodedPassword);
    userRepository.save(user);

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


  /**
   * Authenticates a user with username and password and returns login tokens.
   *
   * @param request the login credentials
   * @return login response with access and refresh tokens
   */
  public LoginResponse login(@Valid LoginRequest request) {
    UserModel user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    return buildLoginResponse(user);
  }


  @Transactional
  public void logout(String token) {
    refreshTokenRepository.deleteByToken(token);
  }

  /**
   * Builds a LoginResponse with fresh tokens for the given user. The refresh token is included here
   * but the controller moves it to an HttpOnly cookie.
   */
  private LoginResponse buildLoginResponse(UserModel user) {
    UserPrincipal principal = new UserPrincipal(user);
    String accessToken = jwtService.generateToken(principal);

    RefreshTokenModel refreshToken = RefreshTokenModel.builder()
        .token(UUID.randomUUID().toString())
        .user(user)
        .expiresAt(Instant.now().plusMillis(refreshExpiration))
        .build();
    refreshTokenRepository.save(refreshToken);

    return userMapper.toLoginResponse(accessToken, refreshToken.getToken());
  }
}
