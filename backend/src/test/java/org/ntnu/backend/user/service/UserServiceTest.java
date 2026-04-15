package org.ntnu.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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

  @InjectMocks
  private UserService userService;

  private UserModel testUser;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(userService, "refreshExpiration", 604800000L);

    testUser = UserModel.builder()
        .id(UUID.randomUUID())
        .username("testuser")
        .email("test@test.no")
        .password("encodedPassword")
        .role(RoleEnum.TEACHER)
        .classrooms(new HashSet<>())
        .build();
  }

  // ===== REGISTER =====

  @Test
  void register_success() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("test@test.no");
    request.setUsername("testuser");
    request.setPassword("password123");
    request.setRole(RoleEnum.TEACHER);

    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userMapper.toUserModel(any(), anyString())).thenReturn(testUser);
    when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("access-token");
    when(refreshTokenRepository.save(any(RefreshTokenModel.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(userMapper.toLoginResponse(anyString(), anyString()))
        .thenReturn(new LoginResponse("access-token", "refresh-token"));

    LoginResponse response = userService.register(request);

    assertThat(response.getAccessToken()).isEqualTo("access-token");
    verify(userRepository).save(testUser);
    verify(refreshTokenRepository).save(any(RefreshTokenModel.class));
  }

  @Test
  void register_duplicateEmail_throws409() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("test@test.no");
    request.setUsername("testuser");

    when(userRepository.existsByEmail("test@test.no")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email");
  }

  @Test
  void register_duplicateUsername_throws409() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("test@test.no");
    request.setUsername("testuser");

    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByUsername("testuser")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("username");
  }

  // ===== LOGIN =====

  @Test
  void login_success() {
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword("password123");

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("access-token");
    when(refreshTokenRepository.save(any(RefreshTokenModel.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(userMapper.toLoginResponse(anyString(), anyString()))
        .thenReturn(new LoginResponse("access-token", "refresh-token"));

    LoginResponse response = userService.login(request);

    assertThat(response.getAccessToken()).isEqualTo("access-token");
  }

  @Test
  void login_wrongUsername_throws401() {
    LoginRequest request = new LoginRequest();
    request.setUsername("unknown");
    request.setPassword("password123");

    when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void login_wrongPassword_throws401() {
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword("wrongpassword");

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
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
    when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("new-access-token");
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

  // ===== JOIN CLASSROOM =====

  @Test
  void joinClassroom_studentSuccess() {
    UserModel student = UserModel.builder()
        .role(RoleEnum.STUDENT)
        .classrooms(new HashSet<>())
        .build();
    ClassroomModel classroom = new ClassroomModel();

    userService.joinClassroom(student, classroom);

    assertThat(student.getClassrooms()).contains(classroom);
  }

  @Test
  void joinClassroom_teacherThrows() {
    UserModel teacher = UserModel.builder()
        .role(RoleEnum.TEACHER)
        .classrooms(new HashSet<>())
        .build();

    assertThatThrownBy(() -> userService.joinClassroom(teacher, new ClassroomModel()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void joinClassroom_alreadyInClassroom_throws() {
    ClassroomModel existing = new ClassroomModel();
    HashSet<ClassroomModel> classrooms = new HashSet<>();
    classrooms.add(existing);

    UserModel student = UserModel.builder()
        .role(RoleEnum.STUDENT)
        .classrooms(classrooms)
        .build();

    assertThatThrownBy(() -> userService.joinClassroom(student, new ClassroomModel()))
        .isInstanceOf(IllegalStateException.class);
  }

  // ===== REGISTER EDGE CASES =====

  @Test
  void register_emailTakenButUsernameAlsoTaken_throwsEmailFirst() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("taken@test.no");
    request.setUsername("taken");

    when(userRepository.existsByEmail("taken@test.no")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email");

    // Verify username check never runs — email fails first
    verify(userRepository, never()).existsByUsername(anyString());
  }

// ===== LOGIN EDGE CASES =====

  @Test
  void login_correctUsernameWrongPassword_doesNotLeakUsernameExists() {
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword("wrong");

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid credentials"); // Same message as unknown user
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

// ===== JOIN CLASSROOM EDGE CASES =====

  @Test
  void joinClassroom_studentJoinsSameClassroomTwice_noDuplicate() {
    ClassroomModel classroom = new ClassroomModel();
    UserModel student = UserModel.builder()
        .role(RoleEnum.STUDENT)
        .classrooms(new HashSet<>())
        .build();

    userService.joinClassroom(student, classroom);
    // Second join should fail — already in a classroom
    assertThatThrownBy(() -> userService.joinClassroom(student, classroom))
        .isInstanceOf(IllegalStateException.class);
  }
}
