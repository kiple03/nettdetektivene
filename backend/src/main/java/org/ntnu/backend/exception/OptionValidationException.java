package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an option fails business-rule validation, such as having no correct answer
 * or containing duplicate option texts.
 *
 * <p>Handled by {@link GlobalExceptionHandler} via the {@link ApiException} base handler,
 * mapped to a {@code 400 Bad Request} response.
 */
public class OptionValidationException extends ApiException {

  /**
   * Constructs the exception with a message describing the validation failure.
   *
   * @param message a description of the validation rule that was violated
   */
  public OptionValidationException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}