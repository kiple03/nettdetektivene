package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a level submission batch is malformed — missing answers,
 * extra answers, unknown question IDs, or contains one or more incorrect
 * answers.
 *
 * <p>Handled by {@link GlobalExceptionHandler} via the {@link ApiException}
 * base handler, mapped to a 400 Bad Request response.
 */
public class InvalidLevelSubmissionException extends ApiException {

  public InvalidLevelSubmissionException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}