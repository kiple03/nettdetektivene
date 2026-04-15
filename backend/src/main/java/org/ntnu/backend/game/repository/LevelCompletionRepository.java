package org.ntnu.backend.game.repository;

import java.util.UUID;
import org.ntnu.backend.level.model.LevelCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelCompletionRepository extends JpaRepository<LevelCompletion, UUID> {}
