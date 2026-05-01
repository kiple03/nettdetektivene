package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a student tries to access a level they haven't unlocked yet. */
public class LevelLockedException extends ApiException {

  /**
   * @param levelId the ID of the locked level
   */
  public LevelLockedException(int levelId) {
    super(HttpStatus.FORBIDDEN, "Level " + levelId + " is locked");
  }
}
