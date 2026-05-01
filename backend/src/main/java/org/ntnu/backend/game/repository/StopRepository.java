package org.ntnu.backend.game.repository;

import java.util.List;
import java.util.Optional;
import org.ntnu.backend.game.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@link Stop} entities.
 *
 * <p>Stops are global and is just a few (7 per the vision doc),
 * Gets data from the db.
 */
@Repository
public interface StopRepository extends JpaRepository<Stop, Integer> {

  /** Returns all stops sorted by {@code displayOrder} ascending. */
  List<Stop> findAllByOrderByDisplayOrderAsc();

  /**
   * Returns the stop immediately after the given display order position.
   *
   * @param displayOrder the current stop's display order
   * @return the next stop, or empty if this is the last stop
   */
  @Query("SELECT s FROM Stop s WHERE s.displayOrder > :displayOrder ORDER BY s.displayOrder ASC LIMIT 1")
  Optional<Stop> findNextStopAfter(int displayOrder);

  /** Returns the first stop (lowest display order). */
  @Query("SELECT s FROM Stop s ORDER BY s.displayOrder ASC LIMIT 1")
  Optional<Stop> findFirstStop();

  /** Returns all stops with their associated level counts as a projection. */
  @Query("SELECT s.id AS stopId, s.displayOrder AS displayOrder, COUNT(l) AS levelCount " +
      "FROM Stop s LEFT JOIN Level l ON l.stop = s " +
      "GROUP BY s.id, s.displayOrder")
  List<StopLevelCount> findAllWithLevelCounts();

  /**
   * Finds a stop by its name.
   *
   * @param name the stop name to look up
   */
  Optional<Stop> findByName(String name);

  /**
   * Returns all stops with the given name, sorted by id ascending.
   *
   * @param name the stop name to filter by
   */
  List<Stop> findAllByNameOrderByIdAsc(String name);
}
