package org.ntnu.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;

class UserPrincipalTest {

    private User user;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getPasswordHash()).thenReturn("$2a$10$hashedpassword");
        when(user.getRole()).thenReturn(RoleEnum.TEACHER);
        userPrincipal = new UserPrincipal(user);
    }

    @Test
    void getUser_returnsSameUserInstance() {
        assertThat(userPrincipal.getUser()).isSameAs(user);
    }

    @Test
    void getUserId_returnsUserId() {
        assertThat(userPrincipal.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void getUsername_returnsEmail() {
        assertThat(userPrincipal.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void getPassword_returnsPasswordHash() {
        assertThat(userPrincipal.getPassword()).isEqualTo("$2a$10$hashedpassword");
    }

    @Test
    void getAuthorities_containsRolePrefixed() {
        assertThat(userPrincipal.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_TEACHER");
    }

    @Test
    void getAuthorities_prefixesStudentRoleCorrectly() {
        when(user.getRole()).thenReturn(RoleEnum.STUDENT);
        UserPrincipal studentPrincipal = new UserPrincipal(user);

        assertThat(studentPrincipal.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_STUDENT");
    }

    @Test
    void isAccountNonExpired_returnsTrue() {
        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLocked_returnsTrue() {
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpired_returnsTrue() {
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isEnabled_returnsTrue() {
        assertThat(userPrincipal.isEnabled()).isTrue();
    }
}