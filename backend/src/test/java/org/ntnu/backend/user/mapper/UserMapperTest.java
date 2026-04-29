package org.ntnu.backend.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toLoginResponse_shouldMapAccessTokenAndRefreshToken() {
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        LoginResponse result = userMapper.toLoginResponse(accessToken, refreshToken);

        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void toLoginResponse_whenTokensAreNull_shouldKeepNullValues() {
        LoginResponse result = userMapper.toLoginResponse(null, null);

        assertThat(result.getAccessToken()).isNull();
        assertThat(result.getRefreshToken()).isNull();
    }

    @Test
    void toUser_shouldMapCreateUserRequestToUserEntity() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("student@example.com");
        request.setRole(RoleEnum.STUDENT);

        String encodedPassword = "$2a$10$encoded-password-hash";

        User result = userMapper.toUser(request, encodedPassword);

        assertThat(result.getEmail()).isEqualTo("student@example.com");
        assertThat(result.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(result.getRole()).isEqualTo(RoleEnum.STUDENT);
    }

    @Test
    void toUser_whenRoleIsTeacher_shouldMapTeacherRole() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("teacher@example.com");
        request.setRole(RoleEnum.TEACHER);

        String encodedPassword = "$2a$10$teacher-password-hash";

        User result = userMapper.toUser(request, encodedPassword);

        assertThat(result.getEmail()).isEqualTo("teacher@example.com");
        assertThat(result.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(result.getRole()).isEqualTo(RoleEnum.TEACHER);
    }

    @Test
    void toUser_shouldStoreEncodedPasswordAsProvided() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("student@example.com");
        request.setRole(RoleEnum.STUDENT);

        String encodedPassword = "already-encoded-value";

        User result = userMapper.toUser(request, encodedPassword);

        assertThat(result.getPasswordHash()).isEqualTo("already-encoded-value");
    }

    @Test
    void toUser_whenEncodedPasswordIsNull_shouldKeepPasswordHashNull() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("student@example.com");
        request.setRole(RoleEnum.STUDENT);

        User result = userMapper.toUser(request, null);

        assertThat(result.getEmail()).isEqualTo("student@example.com");
        assertThat(result.getPasswordHash()).isNull();
        assertThat(result.getRole()).isEqualTo(RoleEnum.STUDENT);
    }
}