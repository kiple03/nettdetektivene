package org.ntnu.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtAuthenticationTokenTest {

    private JwtAuthenticatedPrincipal makePrincipal() {
        return new JwtAuthenticatedPrincipal(
                "user@example.com",
                UUID.randomUUID(),
                RoleEnum.STUDENT,
                UUID.randomUUID(),
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @Test
    void isAuthenticated_returnsTrueAfterConstruction() {
        JwtAuthenticatedPrincipal principal = makePrincipal();
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                principal, principal.getAuthorities()
        );

        assertThat(token.isAuthenticated()).isTrue();
    }

    @Test
    void getCredentials_returnsNull() {
        JwtAuthenticatedPrincipal principal = makePrincipal();
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                principal, principal.getAuthorities()
        );

        assertThat(token.getCredentials()).isNull();
    }

    @Test
    void getPrincipal_returnsSamePrincipal() {
        JwtAuthenticatedPrincipal principal = makePrincipal();
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                principal, principal.getAuthorities()
        );

        assertThat(token.getPrincipal()).isSameAs(principal);
    }

    @Test
    void getAuthorities_containsExpectedRole() {
        JwtAuthenticatedPrincipal principal = makePrincipal();
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                principal, principal.getAuthorities()
        );

        assertThat(token.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_STUDENT");
    }
}