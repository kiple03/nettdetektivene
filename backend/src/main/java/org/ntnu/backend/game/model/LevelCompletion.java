package org.ntnu.backend.game.model;

import java.time.Instant;
import java.util.UUID;

import org.ntnu.backend.classroom.model.Enrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity recording that a student has completed a game level, mapped to the
 * {@code level_completion} table.
 *
 * <p>The combination of {@link #enrollment} and {@link #level} is enforced as unique
 * at the database level, ensuring a student cannot receive credit for the same level
 * more than once within a given classroom enrollment.
 */
@Entity
@Table(
    name = "level_completion",
    uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id", "level_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelCompletion {

    /** Primary key, generated as a random UUID by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The classroom enrollment this completion belongs to. Loaded lazily —
     * use repository {@code JOIN FETCH} queries when the enrollment is needed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    /**
     * The level that was completed. Loaded lazily —
     * use repository {@code JOIN FETCH} queries when the level is needed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    /** Timestamp at which the student completed the level. Set by the service layer before persisting. */
    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}
