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

    /** Projection mapping a user ID to their total medal count. */
    interface UserMedalCount {
        UUID getUserId();
        long getMedalCount();
    }

    /**
     * Returns all medalions owned by a user.
     *
     * @param userId the user's ID
     * @return list of the user's medalions
     */
    List<Medalion> findByUserId(UUID userId);

    /**
     * Checks whether a user already holds a medalion with the given name.
     *
     * @param userId the user's ID
     * @param name   the medalion name
     * @return {@code true} if the medalion exists
     */
    boolean existsByUserIdAndName(UUID userId, String name);

    /**
     * Retrieves a specific medalion by user and name.
     *
     * @param userId the user's ID
     * @param name   the medalion name
     * @return the matching medalion, or {@code null} if not found
     */
    Medalion findByUserIdAndName(UUID userId, String name);

    /**
     * Returns medal counts grouped by user for the given set of user IDs.
     *
     * @param userIds the user IDs to aggregate
     * @return list of {@link UserMedalCount} projections
     */
    @Query("""
        select m.user.id as userId, count(m) as medalCount
        from Medalion m
        where m.user.id in :userIds
        group by m.user.id
        """)
    List<UserMedalCount> countByUserIds(@Param("userIds") List<UUID> userIds);
} 
