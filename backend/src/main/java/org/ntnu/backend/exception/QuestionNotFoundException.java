package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a question is requested by ID but does not exist.
 *
 * <p>Handled by {@link GlobalExceptionHandler} via the {@link ApiException} base handler,
 * mapped to a {@code 404 Not Found} response.
 */
public class QuestionNotFoundException extends ApiException {

  /**
   * Constructs the exception for the given question ID.
   *
   * @param id the ID of the question that could not be found
   */
  public QuestionNotFoundException(int id) {
    super(HttpStatus.NOT_FOUND, "Question not found: " + id);
  }
}