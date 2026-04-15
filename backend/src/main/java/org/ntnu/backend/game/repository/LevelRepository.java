package org.ntnu.backend.game.repository;

import org.ntnu.backend.game.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelRepository extends JpaRepository<Level, Integer> {}