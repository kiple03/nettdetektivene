package org.ntnu.backend.game.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.ntnu.backend.game.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for {@link UserProgress}, providing queries for progress lookup and level counting.
 */
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

  /**
   * Fetches progress records for the given users, eagerly loading {@code currentStop}.
   *
   * @param userIds the user IDs to look up
   * @return list of progress entries with {@code currentStop} populated
   */
  @Query("SELECT p FROM UserProgress p JOIN FETCH p.currentStop WHERE p.user.id IN :userIds")
  List<UserProgress> findByUserIdIn(Collection<UUID> userIds);

  /**
   * Returns the number of levels per stop as {@code [stopId, count]} pairs.
   *
   * @return raw object array rows: {@code [Long stopId, Long levelCount]}
   */
  @Query("SELECT l.stop.id, COUNT(l) FROM Level l GROUP BY l.stop.id")
  List<Object[]> countLevelsByStop();
}
