package org.ntnu.backend.security;

import java.util.Optional;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Persistence layer for refresh tokens, supporting lookup and deletion by token value or user. */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, Long> {

  /**
   * Looks up a refresh token by its string value.
   *
   * @param token the raw token string
   * @return the matching {@link RefreshTokenModel}, if present
   */
  Optional<RefreshTokenModel> findByToken(String token);

  /**
   * Removes a specific refresh token by its string value.
   *
   * @param token the raw token string to delete
   */
  void deleteByToken(String token);

  /**
   * Removes all refresh tokens belonging to the given user (e.g. on logout or account deletion).
   *
   * @param user the user whose tokens should be revoked
   */
  void deleteAllByUser(User user);
}
