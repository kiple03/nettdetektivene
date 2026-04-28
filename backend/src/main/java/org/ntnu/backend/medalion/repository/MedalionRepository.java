package org.ntnu.backend.medalion.repository;

import java.util.List;
import java.util.UUID;

import org.ntnu.backend.medalion.model.Medalion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link Medalion} entities.
 * Provides derived queries for looking up and checking medalions by user and name.
 */
public interface MedalionRepository extends JpaRepository<Medalion, UUID>{
    interface UserMedalCount {
        UUID getUserId();
        long getMedalCount();
    }

    List<Medalion> findByUserId(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
    Medalion findByUserIdAndName(UUID userId, String name);

    @Query("""
        select m.user.id as userId, count(m) as medalCount
        from Medalion m
        where m.user.id in :userIds
        group by m.user.id
        """)
    List<UserMedalCount> countByUserIds(@Param("userIds") List<UUID> userIds);
} 
