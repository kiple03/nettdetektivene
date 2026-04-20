package org.ntnu.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts and validates the JWT from every request Authorization header.
 *
 * <p>Stateless authentication, all data is extracted from
 * the token claims. No database call is made. The resulting {@link JwtAuthenticatedPrincipal} is
 * placed in the SecurityContext for the duration of the request period only.
 *
 * <p>If the token is missing, invalid, or expired, the filter skips authentication and lets
 * Spring Security's authorization rules handle it via {@link JwtAuthenticationEntryPoint}.
 *
 * @author Fredrik Borbe
 * @version 0.1
 * @see JwtService
 * @see JwtAuthenticatedPrincipal
 */
@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  /**
   * Finds the Authorization header field and extracts the jwt string from here.
   *
   * @param request  the incoming HTTP request
   * @param response the response object
   * @param chain    the cain of filers for the request
   * @throws ServletException an error occurs during the processing of the request
   * @throws IOException      an I/O error occurs during the processing of the request
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain
  ) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      authenticateUser(authHeader.substring(7));
    }
    chain.doFilter(request, response);
  }

  /**
   * Extracts the JWT, validates it, and sets the SecurityContext.
   *
   * <p>Builds a {@link JwtAuthenticatedPrincipal} from token claims
   * and wraps it in a custom {@link JwtAuthenticationToken}, since we don't need the credentials
   * fields (it is stateless).
   *
   * @param jwt the token string without the "Bearer " prefix
   */
  private void authenticateUser(String jwt) {
    try {
      Claims claims = jwtService.parseAndValidate(jwt);
      if (SecurityContextHolder.getContext().getAuthentication() != null) return;

      UUID userId = UUID.fromString(claims.get("userId", String.class));
      String classroomIdStr = claims.get("classroomId", String.class);
      UUID classroomId = classroomIdStr != null ? UUID.fromString(classroomIdStr) : null;


      // JWT claim stores the bare role name (e.g. "TEACHER"); Spring Security
      // expects the "ROLE_" prefix on GrantedAuthority, so we add it here.
      String role = claims.get("role", String.class);
      List<SimpleGrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_" + role));

      JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
          claims.getSubject(),
          userId,
          role,
          classroomId,
          authorities
      );

      SecurityContextHolder.getContext().setAuthentication(
          new JwtAuthenticationToken(principal, authorities)
      );
    } catch (JwtException ignored) {}
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return "OPTIONS".equalsIgnoreCase(request.getMethod());
  }
}
