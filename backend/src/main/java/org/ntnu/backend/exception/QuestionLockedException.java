package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a student attempts to access a question without having completed
 * the preceding one, enforcing sequential question progression.
 *
 * <p>Handled by {@link GlobalExceptionHandler} via the {@link ApiException} base handler,
 * mapped to a {@code 403 Forbidden} response.
 */
public class QuestionLockedException extends ApiException {

  /**
   * Constructs the exception with a fixed message instructing the student to complete
   * the previous question first.
   */
  public QuestionLockedException() {
    super(HttpStatus.FORBIDDEN, "You must complete the previous question first");
  }
}