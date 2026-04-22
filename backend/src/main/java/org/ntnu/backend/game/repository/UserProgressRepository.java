package org.ntnu.backend.game.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.ntnu.backend.game.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

  @Query("SELECT p FROM UserProgress p JOIN FETCH p.currentStop WHERE p.user.id IN :userIds")
  List<UserProgress> findByUserIdIn(Collection<UUID> userIds);

  // StopRepository, or a new projection query on LevelRepository
  @Query("SELECT l.stop.id, COUNT(l) FROM Level l GROUP BY l.stop.id")
  List<Object[]> countLevelsByStop();
}
