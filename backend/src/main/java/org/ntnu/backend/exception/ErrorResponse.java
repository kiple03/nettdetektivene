package org.ntnu.backend.exception;

/**
 * Uniform error response body returned by {@link GlobalExceptionHandler} for all
 * handled exceptions.
 *
 * <p>{@code success} is always {@code false} for error responses. {@code field} is
 * populated only for field-level validation errors (e.g. duplicate e-mail on registration)
 * and is {@code null} for general errors.
 *
 * @param success always {@code false}, signals to the client that the request failed
 * @param message a human-readable description of the error
 * @param field   the name of the offending field, or {@code null} if not field-specific
 */
public record ErrorResponse(boolean success, String message, String field) {

  /**
   * Creates a general error response with no associated field.
   *
   * @param message a human-readable description of the error
   * @return an {@code ErrorResponse} with {@code success=false} and {@code field=null}
   */
  public static ErrorResponse of(String message) {
    return new ErrorResponse(false, message, null);
  }

  /**
   * Creates a field-level error response, e.g. for duplicate-value conflicts.
   *
   * @param message a human-readable description of the error
   * @param field   the name of the field that caused the error
   * @return an {@code ErrorResponse} with {@code success=false} and the given field
   */
  public static ErrorResponse of(String message, String field) {
    return new ErrorResponse(false, message, field);
  }
}