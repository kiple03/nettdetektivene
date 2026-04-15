package org.ntnu.backend.level.repository;

import java.util.UUID;
import org.ntnu.backend.level.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRepository extends JpaRepository<Level, UUID> {}
