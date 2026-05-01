package org.ntnu.backend.game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.classroom.model.Enrollment;

/**
 * Records the outcome of a student's boss battle attempt within a classroom enrollment.
 */
@Entity
@Table(name = "boss_battle_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BossBattleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The enrollment this result belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    /** Whether the student won the boss battle. */
    @Column(nullable = false)
    private boolean won;

    /** Total time taken to complete the battle, in seconds. */
    @Column(name = "time_seconds", nullable = false)
    private int timeSeconds;

    /** Player HP remaining at the end of the battle. */
    @Column(name = "player_hp_remaining", nullable = false)
    private int playerHpRemaining;

    /** Timestamp when the battle was completed. */
    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}
