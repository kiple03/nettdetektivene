package org.ntnu.backend.game.repository;

/**
 * Projection interface for querying the number of levels per stop.
 * Used in repository queries to avoid loading full entity graphs.
 */
public interface StopLevelCount {

  /** @return the display order of the stop within the game */
  int getDisplayOrder();

  /** @return the number of levels associated with this stop */
  long getLevelCount();
}
