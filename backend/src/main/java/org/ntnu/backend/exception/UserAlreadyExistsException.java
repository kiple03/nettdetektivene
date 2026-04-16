package org.ntnu.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown during registration when a user with the given email already exists.
 *
 * <p>Handled by {@link GlobalExceptionHandler} and mapped to a 409 Conflict response.
 */
@Getter
public class UserAlreadyExistsException extends ApiException {
  private final String field;

  /**
   * Creates the exception identifying the conflicting email address or username.
   *
   * @param field the conflicting field
   * @param value the value which already exists
   */
  public UserAlreadyExistsException(String field, String value) {
    super(HttpStatus.CONFLICT, "User already exists with " + field + ": " + value);
    this.field = field;
  }
}
