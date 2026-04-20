package org.ntnu.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Abstract base class for all application-specific API exceptions.
 *
 * <p>Subclasses bind a fixed {@link HttpStatus} to a human-readable message at construction
 * time. {@link GlobalExceptionHandler} catches {@code ApiException} and maps it directly to
 * an {@link ErrorResponse} with the corresponding HTTP status code, so subclasses require
 * no additional handler registration.
 *
 * @see GlobalExceptionHandler
 */
@Getter
public abstract class ApiException extends RuntimeException {

  /** The HTTP status code that will be returned to the client when this exception is thrown. */
  private final HttpStatus status;

  /**
   * Constructs an {@code ApiException} with the given HTTP status and message.
   *
   * @param status  the HTTP status to return to the client
   * @param message a human-readable description of the error
   */
  protected ApiException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}