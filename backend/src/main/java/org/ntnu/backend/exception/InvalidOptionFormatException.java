package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an option's stored JSON cannot be parsed into the expected format.
 *
 * <p>Handled by {@link GlobalExceptionHandler} via the {@link ApiException} base handler,
 * mapped to a {@code 400 Bad Request} response.
 *
 */
public class InvalidOptionFormatException extends ApiException {

  /**
   * Constructs the exception with a message describing the format violation.
   *
   * @param message a description of why the option format is invalid
   */
  public InvalidOptionFormatException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}