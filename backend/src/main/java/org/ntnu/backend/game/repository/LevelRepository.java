package org.ntnu.backend.game.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ntnu.backend.game.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRepository extends JpaRepository<Level, Integer> {
  List<Level> findByStopIdOrderByIdAsc(int stopId);

  long countByStopId(int id);

  Optional<Level> findByStopIdAndTitle(int stopId, String title);
}