package org.ntnu.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Handles JWT creation, validation and extractions of user claims.
 *
 * <p>Each token lives for 5 mins (set in env vars), and will carry the users identity,
 * organization and roles (used for permissions) stored as claims. This allows the
 * {@link JwtAuthFilter} to authenticate the user without a database call on every request</p>
 *
 * <p>Token structure:
 * <pre>
 * {
 * }
 * </pre>
 *
 * @see JwtAuthFilter
 * @see JwtAuthenticatedPrincipal
 */
@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-expiration-ms}")
  private long expiration;

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Generates an JWT containing user claims.
   *
   * <p>Roles are stored as authority strings (prefixed with "ROLE_") the user is granted,
   * so the filter can create Spring Security authorities directly from the token. If the org is not
   * set (like on signup), the field is set to null.</p>
   *
   * <p>Only STUDENTS will have their classroom id encoded, since they only belong in one classroom,
   * and is used to pull e.g. custom questions a teacher has added to a classroom.</p>
   *
   * @param principal the authenticated user
   * @return a JWT string
   */
  public String generateToken(UserPrincipal principal) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", principal.getUser().getId().toString());
    claims.put("role", principal.getAuthorities().iterator().next().getAuthority());

    if (principal.getUser().getRole() == RoleEnum.STUDENT) {
      Set<Classroom> classrooms = principal.getUser().getClassrooms();
      if (!classrooms.isEmpty()) {
        claims.put("classroomId", classrooms.iterator().next().getId().toString());
      }
    }

    return Jwts.builder()
        .claims(claims)
        .subject(principal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  public Claims parseAndValidate(String token) {
    return getClaims(token);
  }


  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }
}
