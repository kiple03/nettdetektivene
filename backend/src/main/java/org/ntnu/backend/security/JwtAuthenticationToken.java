package org.ntnu.backend.security;


import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authentication token created from validated JWT claims.
 *
 * <p>{@code setAuthenticated(true)} is called in the constructor because
 * this token is only instantiated by {@link JwtAuthFilter} after the JWT signature and expiration
 * have been verified by {@link JwtService}.</p>
 *
 * @see JwtAuthFilter
 * @see JwtAuthenticatedPrincipal
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  private final JwtAuthenticatedPrincipal principal;

  /**
   * Creates an already-authenticated token from a verified JWT principal.
   *
   * @param principal the principal extracted from the validated JWT
   * @param authorities the granted authorities derived from the token's role claims
   */
  public JwtAuthenticationToken(JwtAuthenticatedPrincipal principal,
      Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  /**
   * Returns {@code null}, this token is stateless and carries no credentials.
   *
   * @return always {@code null}
   */
  @Override
  public Object getCredentials() {
    return null;
  }

  /**
   * Returns the JWT-derived principal for this authentication.
   *
   * @return the {@link JwtAuthenticatedPrincipal} extracted from the token
   */
  @Override
  public JwtAuthenticatedPrincipal getPrincipal() {
    return principal;
  }
}