package org.ntnu.backend.config;

import java.util.List;
import lombok.AllArgsConstructor;
import org.ntnu.backend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Central security configuration for the backend.
 *
 * <p>Implements a stateless session, no server-side session storage, we only need the JWT to
 * authenticate each request. CSRF is disabled, since the authentication is token based, not relying
 * on sessions or cookies. Method-level security enabled via {@code @EnableMethodSecurity} for
 * {@code @PreAuthorize} checks in controllers.
 *
 * <p>URL-level rules define broad access patterns. Individual endpoints
 * use {@code @PreAuthorize} for role-specific restrictions:
 *
 * <pre>
 * &#64;@PreAuthorize("hasRole('TEACHER')")
 * &#64;PostMapping("/classrooms")
 * public ResponseEntity&lt;T&gt; showClassrooms(...) { }
 * </pre>
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private AuthenticationEntryPoint jwtAuthenticationEntryPoint;


  /**
   * Provides a BCrypt password encoder bean used for hashing and verifying passwords.
   *
   * @return a {@link BCryptPasswordEncoder} instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configures the HTTP security filter chain.
   *
   * <p>Registers the {@link JwtAuthFilter} before the default username/password filter,
   * sets sessions to stateless, disables CSRF (since we use jwt), and defines URL-level access
   * rules. Auth and Swagger endpoints are publicly accessible, all other requests require
   * authentication.
   *
   * @param http the {@link HttpSecurity} builder
   * @return the built {@link SecurityFilterChain}
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
    return http.build();
  }

  /**
   * Configures CORS to allow requests from the frontend origins.
   *
   * <p>Allows all headers and the standard REST methods. Credentials are allowed
   * so that the HttpOnly refresh-token cookie is sent on cross-origin requests.
   *
   * @return the CORS configuration source
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }


}

