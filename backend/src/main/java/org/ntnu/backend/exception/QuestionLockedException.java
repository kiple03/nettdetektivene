package org.ntnu.backend.exception;

public class QuestionLockedException extends RuntimeException {

  public QuestionLockedException(String message) {
    super(message);
  }
}
