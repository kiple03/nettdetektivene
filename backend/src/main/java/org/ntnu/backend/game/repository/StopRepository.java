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

    // TODO: use instead of findALl
    List<Stop> findAllByOrderByDisplayOrderAsc();

  @Query("SELECT s FROM Stop s WHERE s.displayOrder > :displayOrder ORDER BY s.displayOrder ASC LIMIT 1")
  Optional<Stop> findNextStopAfter(int displayOrder);

  @Query("SELECT s FROM Stop s ORDER BY s.displayOrder ASC LIMIT 1")
  Optional<Stop> findFirstStop();

  @Query("SELECT s.id AS stopId, s.displayOrder AS displayOrder, COUNT(l) AS levelCount " +
      "FROM Stop s LEFT JOIN Level l ON l.stop = s " +
      "GROUP BY s.id, s.displayOrder")
  List<StopLevelCount> findAllWithLevelCounts();

    Optional<Stop> findByName(String name);

    List<Stop> findAllByNameOrderByIdAsc(String name);
}
