package org.ntnu.backend.medalion.repository;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.medalion.model.Medalion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedalionRepository extends JpaRepository<Medalion, UUID>{
    List<Medalion> findByUserId(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
    Medalion findByUserIdAndName(UUID userId, String name);
} 
