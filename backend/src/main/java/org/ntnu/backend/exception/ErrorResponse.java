package org.ntnu.backend.exception;

public record ErrorResponse(boolean success, String message, String field) {
  public static ErrorResponse of(String message) {
    return new ErrorResponse(false, message, null);
  }
  public static ErrorResponse of(String message, String field) {
    return new ErrorResponse(false, message, field);
  }
}
