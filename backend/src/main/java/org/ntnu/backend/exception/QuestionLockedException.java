package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

public class QuestionLockedException extends ApiException {
  public QuestionLockedException() {
    super(HttpStatus.FORBIDDEN, "You must complete the previous question first");
  }
}
