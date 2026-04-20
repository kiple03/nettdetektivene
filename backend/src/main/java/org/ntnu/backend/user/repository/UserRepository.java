package org.ntnu.backend.user.repository;

import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link User} entities, keyed by {@link UUID}.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination operations.
 * The custom query methods below are resolved automatically by Spring Data JPA from
 * their method names.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

  /**
   * Looks up a user by their e-mail address.
   *
   * @param email the e-mail address to search for
   * @return an {@link Optional} containing the matching user, or empty if none found
   */
  Optional<User> findByEmail(String email);

  /**
   * Looks up a user by their username.
   *
   * @param username the username to search for
   * @return an {@link Optional} containing the matching user, or empty if none found
   */
  Optional<User> findByUsername(String username);

  /**
   * Checks whether a user with the given e-mail address already exists.
   *
   * @param email the e-mail address to check
   * @return {@code true} if at least one user with this e-mail exists, {@code false} otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Checks whether a user with the given username already exists.
   *
   * @param username the username to check
   * @return {@code true} if at least one user with this username exists, {@code false} otherwise
   */
  boolean existsByUsername(String username);
}
