package org.ntnu.backend.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class QuestionNotFoundException extends ApiException {
  public QuestionNotFoundException(UUID id) {
    super(HttpStatus.NOT_FOUND, "Question not found: " + id);
  }
}
