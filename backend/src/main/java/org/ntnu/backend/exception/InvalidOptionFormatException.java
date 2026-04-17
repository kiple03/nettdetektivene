package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidOptionFormatException extends ApiException {
  public InvalidOptionFormatException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}