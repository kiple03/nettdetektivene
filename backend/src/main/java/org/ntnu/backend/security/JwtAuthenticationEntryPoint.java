package org.ntnu.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Returns a 401 JSON response when an unauthenticated request reaches a protected endpoint.
 *
 * <p>Registered in {@link edu.ntnu.idatt2105.backend.config.SecurityConfig} as the entry point
 * for authentication failures. Produces a consistent JSON error body instead of Spring's default
 * HTML error page.
 *
 * @see edu.ntnu.idatt2105.backend.config.SecurityConfig
 */
@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  /**
   * Writes a 401 Unauthorized JSON response.
   *
   * @param request the request that failed authentication
   * @param response the response to write the error to
   * @param authException the exception describing why authentication failed
   * @throws IOException if writing the response fails
   */
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    Map<String, Object> body = Map.of(
        "success", false,
        "message", "Unauthorized: " + authException.getMessage()
    );

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}