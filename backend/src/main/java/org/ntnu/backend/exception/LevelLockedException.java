package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

public class LevelLockedException extends ApiException {
  public LevelLockedException(int levelId) {
    super(HttpStatus.FORBIDDEN, "Level " + levelId + " is locked");
  }
}
