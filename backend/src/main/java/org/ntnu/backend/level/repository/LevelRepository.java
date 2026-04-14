package org.ntnu.backend.level.repository;

import org.ntnu.backend.level.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelRepository extends JpaRepository<Level, Integer> {}