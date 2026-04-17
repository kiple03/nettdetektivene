package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

public class OptionValidationException extends ApiException {

  public OptionValidationException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
