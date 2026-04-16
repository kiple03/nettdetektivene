package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a level is requested by ID but does not exist.
 *
 * <p>Handled by {@link GlobalExceptionHandler} via the {@link ApiException}
 * base handler, mapped to a 404 Not Found response.
 */
public class LevelNotFoundException extends ApiException {

  public LevelNotFoundException(int id) {
    super(HttpStatus.NOT_FOUND, "Level not found: " + id);
  }
}