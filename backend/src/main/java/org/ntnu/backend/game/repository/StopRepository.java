package org.ntnu.backend.game.repository;

import java.util.List;
import org.ntnu.backend.game.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
