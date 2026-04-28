package org.ntnu.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.security.JwtAuthFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        JwtAuthFilter jwtAuthFilter = mock(JwtAuthFilter.class);
        org.springframework.security.web.AuthenticationEntryPoint entryPoint =
                mock(org.springframework.security.web.AuthenticationEntryPoint.class);
        securityConfig = new SecurityConfig(jwtAuthFilter, entryPoint);
    }

    // --- helper: extract the CorsConfiguration registered for "/**" directly ---
    private CorsConfiguration corsConfig() {
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();
        // getCorsConfigurations() returns the raw pattern→config map, no request needed
        return source.getCorsConfigurations().get("/**");
    }

    @Test
    void passwordEncoder_returnsBCryptEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void passwordEncoder_encodesAndMatchesPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "testPassword123";

        String encoded = encoder.encode(raw);

        assertThat(encoder.matches(raw, encoded)).isTrue();
        assertThat(encoder.matches("wrongPassword", encoded)).isFalse();
    }

    @Test
    void corsConfigurationSource_hasCatchAllPattern() {
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();

        assertThat(source.getCorsConfigurations()).containsKey("/**");
    }

    @Test
    void corsConfigurationSource_allowsLocalhost5173() {
        assertThat(corsConfig().getAllowedOrigins()).contains("http://localhost:5173");
    }

    @Test
    void corsConfigurationSource_allowsStandardHttpMethods() {
        assertThat(corsConfig().getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    void corsConfigurationSource_allowsCredentials() {
        assertThat(corsConfig().getAllowCredentials()).isTrue();
    }

    @Test
    void corsConfigurationSource_allowsAllHeaders() {
        assertThat(corsConfig().getAllowedHeaders()).contains("*");
    }
}