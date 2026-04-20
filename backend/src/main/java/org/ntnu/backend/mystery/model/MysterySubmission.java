package org.ntnu.backend.mystery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.ntnu.backend.mystery.model.enums.MysteryStatusEnum;
import org.ntnu.backend.user.model.User;

/**
 * JPA entity representing a student's mystery submission, mapped to the
 * {@code mystery_submission} table.
 *
 * <p>A submission is tied to a classroom via {@link #enrollment} and progresses through
 * statuses defined in {@link MysteryStatusEnum}. Review fields ({@link #reviewedByTeacher},
 * {@link #reviewedAt}) are populated only once a teacher has acted on the submission.
 */
@Entity
@Table(name = "mystery_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MysterySubmission {

    /** Primary key, generated as a random UUID by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The classroom enrollment this submission belongs to.
     * Loaded lazily — use repository {@code JOIN FETCH} queries when the enrollment is needed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    /** The student's written submission text. Stored as {@code TEXT} to allow long entries. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Optional URL of an image attached to the submission. {@code null} if no image was uploaded. */
    @Column(name = "image_url")
    private String imageUrl;

    /** Timestamp at which the student submitted. Set by the service layer before persisting. */
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    /**
     * Current review status of the submission. Defaults to {@link MysteryStatusEnum#PENDING}
     * on creation via the Lombok builder.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MysteryStatusEnum status = MysteryStatusEnum.PENDING;

    /**
     * The teacher who reviewed this submission. {@code null} until a review action is taken.
     * Loaded lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_teacher_id")
    private User reviewedByTeacher;

    /** Timestamp of the review action. {@code null} until a teacher reviews the submission. */
    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /** Whether this submission has been featured/highlighted by a teacher in the classroom. */
    @Column(name = "is_featured", nullable = false)
    private boolean featured;
}
