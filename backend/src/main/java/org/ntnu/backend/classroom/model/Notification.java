package org.ntnu.backend.classroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.ntnu.backend.classroom.model.enums.NotificationType;

/**
 * A notification sent to a teacher about activity in one of their classrooms
 * (e.g. a student joining or submitting a mystery answer).
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Teacher who receives this notification. */
    @Column(nullable = false)
    private UUID teacherId;

    /** Classroom the event originated in. */
    @Column(nullable = false)
    private UUID classroomId;

    /** Student whose action triggered the notification. */
    @Column(nullable = false)
    private UUID studentId;

    /** Categorises what kind of event this notification represents. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private NotificationType type;

    /** Human-readable description of the event, shown in the teacher UI. */
    @Column(nullable = false, length = 512)
    private String message;

    /** {@code true} once the teacher has viewed/dismissed the notification. */
    @Column(name = "is_read", nullable = false)
    private boolean read;

    /** Timestamp set automatically when the record is first persisted. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
