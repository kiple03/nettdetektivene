package org.ntnu.backend.exception;

public class InvalidOptionFormatException extends RuntimeException {
  public InvalidOptionFormatException(String message) {
    super(message);
  }
}