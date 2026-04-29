package org.ntnu.backend.exception;

import org.springframework.http.HttpStatus;

public class BossLockedException extends ApiException {

  public BossLockedException() {
    super(HttpStatus.FORBIDDEN, "You have not unlocked the boss stop yet" );
  }
}
