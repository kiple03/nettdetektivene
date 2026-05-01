package org.ntnu.backend.game.repository;

import java.util.Optional;
import java.util.UUID;

import org.ntnu.backend.game.model.BossBattleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for {@link BossBattleResult} entities. */
@Repository
public interface BossBattleResultRepository extends JpaRepository<BossBattleResult, UUID> {

    /**
     * Returns the most recent boss battle result for the given enrollment.
     *
     * @param enrollmentId the enrollment's UUID
     * @return the latest result, or empty if none exists
     */
    Optional<BossBattleResult> findTopByEnrollmentIdOrderByCompletedAtDesc(UUID enrollmentId);
}
