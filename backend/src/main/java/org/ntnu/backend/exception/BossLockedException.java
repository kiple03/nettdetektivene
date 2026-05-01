package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a student attempts to access the boss stop before completing the required levels. */
public class BossLockedException extends ApiException {

  /** Creates the exception with a fixed 403 Forbidden status. */
  public BossLockedException() {
    super(HttpStatus.FORBIDDEN, "You have not unlocked the boss stop yet" );
  }
}
