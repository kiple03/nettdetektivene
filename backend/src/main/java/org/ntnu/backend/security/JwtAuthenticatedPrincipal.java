package org.ntnu.backend.security;


import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lightweight principal constructed from JWT claims.
 *
 * <p>Unlike {@link UserPrincipal}, which wraps a full User entity loaded from the database, this
 * class only carry the data embedded in the JWT. It is created by {@link JwtAuthFilter} on every
 * request and placed in the SecurityContext.</p>
 *
 * @see JwtAuthFilter
 */
@Getter
@Setter
@AllArgsConstructor
public class JwtAuthenticatedPrincipal {

  private final String email;
  private final UUID userId;
  private final String role;
  private final UUID classroomId;
  private final Collection<? extends GrantedAuthority> authorities;

  /**
   * Extracts the principal from a Spring Security {@link Authentication} object.
   *
   * @param auth the current authentication, may be {@code null}
   * @return the authenticated principal
   * @throws org.springframework.web.server.ResponseStatusException 401 if auth is missing or not
   *                                                                JWT-based
   */
  public static JwtAuthenticatedPrincipal from(Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof JwtAuthenticatedPrincipal principal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
    return principal;
  }

  public UUID requireClassroomId() {
    if (classroomId == null) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No classroom assigned");
    }
    return classroomId;
    }

  public boolean isTeacher() {
    return "TEACHER".equals(role);
  }

  public boolean isStudent() {
    return "STUDENT".equals(role);
  }
}
