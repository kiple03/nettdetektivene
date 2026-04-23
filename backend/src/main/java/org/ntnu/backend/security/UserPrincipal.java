package org.ntnu.backend.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.ntnu.backend.user.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Links the {@link User} entity to Spring Security's {@link UserDetails}.
 *
 * <p>Used only during login to verify credentials and extract roles
 * for JWT generation. Not used on the rest og the requests, those use {@link
 * JwtAuthenticatedPrincipal} instead, to keep the authentication stateless
 */
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

  private final User user;

  /**
   * Returns the {@link User} object
   *
   * @return the User
   */
  public User getUser() {
    return user;
  }

  /**
   * Returns the user's UUID primary key.
   *
   * @return the user ID
   */
  public UUID getUserId() {
    return user.getId();
  }


  /**
   * Returns the user's email address, used as the Spring Security username.
   *
   * @return the user's email
   */
  @Override
  public String getUsername() {
    return user.getEmail();
  }

  /**
   * Returns the bcrypt-hashed password stored for this user.
   *
   * @return the encoded password
   */
  @Override
  public @Nullable String getPassword() {
    return user.getPasswordHash();
  }

  /**
   * Converts the user's roles to Spring Security {@link GrantedAuthority} objects.
   *
   * <p>Each role name is prefixed with {@code "ROLE_"} so that
   * {@code @PreAuthorize("hasRole('MANAGER')")} resolves correctly.
   *
   * @return the set of granted authorities derived from the user's roles
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }
}
