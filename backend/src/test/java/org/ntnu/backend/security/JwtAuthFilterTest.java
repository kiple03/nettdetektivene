package org.ntnu.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        jwtAuthFilter = new JwtAuthFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_setsAuthenticationForValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UUID userId = UUID.randomUUID();
        Claims claims = mockClaims("user@example.com", userId, "TEACHER", null);
        when(jwtService.parseAndValidate("valid.jwt.token")).thenReturn(claims);

        request.addHeader("Authorization", "Bearer valid.jwt.token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_setsCorrectEmailAsPrincipal() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UUID userId = UUID.randomUUID();
        Claims claims = mockClaims("teacher@school.no", userId, "TEACHER", null);
        when(jwtService.parseAndValidate(anyString())).thenReturn(claims);

        request.addHeader("Authorization", "Bearer valid.jwt.token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        JwtAuthenticationToken auth = (JwtAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal().getEmail()).isEqualTo("teacher@school.no");
    }

    @Test
    void doFilterInternal_parsesClassroomIdWhenPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UUID userId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        Claims claims = mockClaims("student@school.no", userId, "STUDENT", classroomId.toString());
        when(jwtService.parseAndValidate(anyString())).thenReturn(claims);

        request.addHeader("Authorization", "Bearer valid.jwt.token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        JwtAuthenticationToken auth = (JwtAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal().getClassroomId()).isEqualTo(classroomId);
    }

    @Test
    void doFilterInternal_setsClassroomIdToNullWhenAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mockClaims("teacher@school.no", UUID.randomUUID(), "TEACHER", null);
        when(jwtService.parseAndValidate(anyString())).thenReturn(claims);

        request.addHeader("Authorization", "Bearer valid.jwt.token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        JwtAuthenticationToken auth = (JwtAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal().getClassroomId()).isNull();
    }

    @Test
    void doFilterInternal_doesNotAuthenticateWhenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).parseAndValidate(anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doesNotAuthenticateWhenHeaderLacksBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).parseAndValidate(anyString());
    }

    @Test
    void doFilterInternal_skipsAuthWhenJwtExceptionThrown() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.parseAndValidate(anyString())).thenThrow(new JwtException("expired"));
        request.addHeader("Authorization", "Bearer expired.token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsAuthWhenRoleClaimIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mockClaims("user@test.no", UUID.randomUUID(), "INVALID_ROLE", null);
        when(jwtService.parseAndValidate(anyString())).thenReturn(claims);

        request.addHeader("Authorization", "Bearer token.with.bad.role");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_skipsAuthWhenRoleClaimIsNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mockClaims("user@test.no", UUID.randomUUID(), null, null);
        when(jwtService.parseAndValidate(anyString())).thenReturn(claims);

        request.addHeader("Authorization", "Bearer token.with.null.role");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_doesNotOverwriteExistingAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        JwtAuthenticatedPrincipal existing = new JwtAuthenticatedPrincipal(
                "existing@test.no", UUID.randomUUID(),
                org.ntnu.backend.user.model.enums.RoleEnum.TEACHER,
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(existing, existing.getAuthorities())
        );

        Claims claims = mockClaims("other@test.no", UUID.randomUUID(), "STUDENT", null);
        when(jwtService.parseAndValidate(anyString())).thenReturn(claims);
        request.addHeader("Authorization", "Bearer some.token");

        jwtAuthFilter.doFilterInternal(request, response, chain);

        JwtAuthenticationToken auth = (JwtAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal().getEmail()).isEqualTo("existing@test.no");
    }

    @Test
    void shouldNotFilter_returnsTrueForOptionsMethod() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/test");

        assertThat(jwtAuthFilter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_returnsFalseForGetMethod() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        assertThat(jwtAuthFilter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void shouldNotFilter_returnsFalseForPostMethod() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");

        assertThat(jwtAuthFilter.shouldNotFilter(request)).isFalse();
    }

    private Claims mockClaims(String subject, UUID userId, String role, String classroomId) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(subject);
        when(claims.get("userId", String.class)).thenReturn(userId != null ? userId.toString() : null);
        when(claims.get("role", String.class)).thenReturn(role);
        when(claims.get("classroomId", String.class)).thenReturn(classroomId);
        return claims;
    }
}