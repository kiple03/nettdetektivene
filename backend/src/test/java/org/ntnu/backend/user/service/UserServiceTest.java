package org.ntnu.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.avatar.service.UserAvatarService;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.ntnu.backend.medalion.service.MedalionService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtService jwtService;
  @Mock
  private UserMapper userMapper;
  @Mock
  private RefreshTokenRepository refreshTokenRepository;
  @Mock
  private EnrollmentRepository enrollmentRepository;
  @Mock
  private UserAvatarService userAvatarService;
  @Mock 
  private MedalionService medalionService;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(userService, "refreshExpiration", 604800000L);

    testUser = User.builder()
        .id(UUID.randomUUID())
        .username("testuser")
        .email("test@test.no")
        .passwordHash("encodedPassword")
        .role(RoleEnum.TEACHER)
        .build();
  }

  // ===== REGISTER =====

  @Test
  void register_success() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("test@test.no");
    request.setPassword("password123");
    request.setRole(RoleEnum.TEACHER);

    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userMapper.toUser(any(), anyString())).thenReturn(testUser);
    when(jwtService.generateToken(any(UserPrincipal.class), any())).thenReturn("access-token");
    when(refreshTokenRepository.save(any(RefreshTokenModel.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(userMapper.toLoginResponse(anyString(), anyString()))
        .thenReturn(new LoginResponse("access-token", "refresh-token"));

    when(userRepository.save(any(User.class))).thenReturn(testUser);
    LoginResponse response = userService.register(request);

    assertThat(response.getAccessToken()).isEqualTo("access-token");
    verify(userRepository).save(testUser);
    verify(refreshTokenRepository).save(any(RefreshTokenModel.class));
  }

  @Test
  void register_duplicateEmail_throws409() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("test@test.no");

    when(userRepository.existsByEmail("test@test.no")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email");
  }

  // ===== LOGIN =====

  @Test
  void login_success() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@test.no");
    request.setPassword("password123");
    request.setPortalRole(RoleEnum.TEACHER);

    when(userRepository.findByEmail("test@test.no")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(jwtService.generateToken(any(UserPrincipal.class), any())).thenReturn("access-token");
    when(refreshTokenRepository.save(any(RefreshTokenModel.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(userMapper.toLoginResponse(anyString(), anyString()))
        .thenReturn(new LoginResponse("access-token", "refresh-token"));

    LoginResponse response = userService.login(request);

    assertThat(response.getAccessToken()).isEqualTo("access-token");
  }

  @Test
  void login_wrongEmail_throws401() {
    LoginRequest request = new LoginRequest();
    request.setEmail("unknown@test.no");
    request.setPassword("password123");
    request.setPortalRole(RoleEnum.TEACHER);

    when(userRepository.findByEmail("unknown@test.no")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void login_wrongPassword_throws401() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@test.no");
    request.setPassword("wrongpassword");
    request.setPortalRole(RoleEnum.TEACHER);

    when(userRepository.findByEmail("test@test.no")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BadCredentialsException.class);
  }

  // ===== REFRESH =====

  @Test
  void refresh_success() {
    RefreshTokenModel refreshToken = RefreshTokenModel.builder()
        .token("valid-token")
        .user(testUser)
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();

    when(refreshTokenRepository.findByToken("valid-token"))
        .thenReturn(Optional.of(refreshToken));
    when(jwtService.generateToken(any(UserPrincipal.class), any())).thenReturn("new-access-token");
    when(refreshTokenRepository.save(any(RefreshTokenModel.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(userMapper.toLoginResponse(anyString(), anyString()))
        .thenReturn(new LoginResponse("new-access-token", "new-refresh-token"));

    LoginResponse response = userService.refresh("valid-token");

    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    verify(refreshTokenRepository).delete(refreshToken);
    verify(refreshTokenRepository).save(any(RefreshTokenModel.class));
  }

  @Test
  void refresh_invalidToken_throws401() {
    when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.refresh("invalid"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("Invalid refresh token");
  }

  @Test
  void refresh_expiredToken_deletesAndThrows() {
    RefreshTokenModel expired = RefreshTokenModel.builder()
        .token("expired-token")
        .user(testUser)
        .expiresAt(Instant.now().minusSeconds(3600))
        .build();

    when(refreshTokenRepository.findByToken("expired-token"))
        .thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> userService.refresh("expired-token"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("expired");

    verify(refreshTokenRepository).delete(expired);
  }

  // ===== LOGOUT =====

  @Test
  void logout_deletesToken() {
    userService.logout("some-token");
    verify(refreshTokenRepository).deleteByToken("some-token");
  }

  // ===== REGISTER EDGE CASES =====

  @Test
  void register_emailTaken_throws409() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("taken@test.no");

    when(userRepository.existsByEmail("taken@test.no")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email");
  }

  // ===== LOGIN EDGE CASES =====

  @Test
  void login_correctEmailWrongPassword_doesNotLeakEmailExists() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@test.no");
    request.setPassword("wrong");
    request.setPortalRole(RoleEnum.TEACHER);

    when(userRepository.findByEmail("test@test.no")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid credentials");
  }

  @Test
  void login_wrongPortalRole_throws403() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@test.no");
    request.setPassword("password123");
    request.setPortalRole(RoleEnum.STUDENT); // testUser is TEACHER

    when(userRepository.findByEmail("test@test.no")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("portal");
  }

  // ===== REFRESH EDGE CASES =====

  @Test
  void refresh_tokenExpiredJustNow_throws() {
    RefreshTokenModel justExpired = RefreshTokenModel.builder()
        .token("just-expired")
        .user(testUser)
        .expiresAt(Instant.now().minusMillis(1))
        .build();

    when(refreshTokenRepository.findByToken("just-expired"))
        .thenReturn(Optional.of(justExpired));

    assertThatThrownBy(() -> userService.refresh("just-expired"))
        .isInstanceOf(BadCredentialsException.class);
  }
}
