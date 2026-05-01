package org.ntnu.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

class JwtAuthenticatedPrincipalTest {

    private JwtAuthenticatedPrincipal principal(RoleEnum role, UUID classroomId) {
        return new JwtAuthenticatedPrincipal(
                "user@example.com",
                UUID.randomUUID(),
                role,
                classroomId,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }

    @Test
    void from_returnsCorrectPrincipalWhenAuthIsValid() {
        JwtAuthenticatedPrincipal p = principal(RoleEnum.TEACHER, null);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(p);

        JwtAuthenticatedPrincipal result = JwtAuthenticatedPrincipal.from(auth);

        assertThat(result).isSameAs(p);
    }

    @Test
    void from_throwsUnauthorizedWhenAuthIsNull() {
        assertThatThrownBy(() -> JwtAuthenticatedPrincipal.from(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void from_throwsUnauthorizedWhenPrincipalIsNotJwtType() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("not-a-principal");

        assertThatThrownBy(() -> JwtAuthenticatedPrincipal.from(auth))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void requireClassroomId_returnsIdWhenPresent() {
        UUID classroomId = UUID.randomUUID();
        JwtAuthenticatedPrincipal p = principal(RoleEnum.STUDENT, classroomId);

        assertThat(p.requireClassroomId()).isEqualTo(classroomId);
    }

    @Test
    void requireClassroomId_throwsForbiddenWhenNull() {
        JwtAuthenticatedPrincipal p = principal(RoleEnum.TEACHER, null);

        assertThatThrownBy(p::requireClassroomId)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void isTeacher_returnsTrueForTeacherRole() {
        assertThat(principal(RoleEnum.TEACHER, null).isTeacher()).isTrue();
    }

    @Test
    void isTeacher_returnsFalseForStudentRole() {
        assertThat(principal(RoleEnum.STUDENT, null).isTeacher()).isFalse();
    }

    @Test
    void isStudent_returnsTrueForStudentRole() {
        assertThat(principal(RoleEnum.STUDENT, null).isStudent()).isTrue();
    }

    @Test
    void isStudent_returnsFalseForTeacherRole() {
        assertThat(principal(RoleEnum.TEACHER, null).isStudent()).isFalse();
    }

    @Test
    void getters_returnCorrectValues() {
        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));

        JwtAuthenticatedPrincipal p = new JwtAuthenticatedPrincipal(
                "test@example.com", userId, RoleEnum.STUDENT, classroomId, authorities
        );

        assertThat(p.getEmail()).isEqualTo("test@example.com");
        assertThat(p.getUserId()).isEqualTo(userId);
        assertThat(p.getRole()).isEqualTo(RoleEnum.STUDENT);
        assertThat(p.getClassroomId()).isEqualTo(classroomId);
        assertThat(p.getAuthorities()).isEqualTo(authorities);
    }
}