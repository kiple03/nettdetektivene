package org.ntnu.backend.game.repository;

import java.util.List;
import org.ntnu.backend.game.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StopRepository extends JpaRepository<Stop, Integer> {
} 
