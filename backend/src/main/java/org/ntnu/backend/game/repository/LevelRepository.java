package org.ntnu.backend.game.repository;

import java.util.List;
import java.util.Optional;
import org.ntnu.backend.game.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for {@link Level} entities. */
@Repository
public interface LevelRepository extends JpaRepository<Level, Integer> {

  /**
   * Returns all levels for a stop, sorted by id ascending.
   *
   * @param stopId the stop id
   */
  List<Level> findByStopIdOrderByIdAsc(int stopId);

  /**
   * Returns all levels for a stop with a given title, sorted by id ascending.
   *
   * @param stopId the stop id
   * @param title  the level title
   */
  List<Level> findAllByStopIdAndTitleOrderByIdAsc(int stopId, String title);

  /**
   * Counts the number of levels belonging to a stop.
   *
   * @param id the stop id
   */
  long countByStopId(int id);

  /**
   * Finds a level by stop and title.
   *
   * @param stopId the stop id
   * @param title  the level title
   */
  Optional<Level> findByStopIdAndTitle(int stopId, String title);

  /**
   * Finds the first (lowest id) level for a stop with the given title.
   *
   * @param stopId the stop id
   * @param title  the level title
   */
  Optional<Level> findTopByStopIdAndTitleOrderByIdAsc(int stopId, String title);

  /**
   * Returns all levels for a stop sorted by their display order.
   *
   * @param stopId the stop id
   */
  List<Level> findByStopIdOrderByOrderAsc(int stopId);
}
