package org.ntnu.backend.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a student's progress row is requested but does not exist.
 *
 * <p>Every student should have a {@code UserProgress} row created at registration,
 * so this exception indicates either a data integrity issue or that the lookup
 * was performed for a non-student user.</p>
 */
public class UserProgressNotFoundException extends ApiException {

  public UserProgressNotFoundException(UUID userId) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "User progress not found for user: " + userId);
  }
}