package org.ntnu.backend.classroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.ntnu.backend.user.model.User;

/**
 * Join entity representing the many-to-many relationship between a {@link Classroom} and a
 * teacher {@link User}. Tracks whether the teacher is the classroom owner or a co-teacher,
 * and when the association was created.
 */
@Entity
@Table(name = "classroom_teacher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomTeacher {

    /** Composite primary key consisting of classroom ID and user ID. */
    @EmbeddedId
    private ClassroomTeacherId id;

    /** The classroom this teacher is associated with. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("classroomId")
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /** The teacher user associated with this classroom. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** {@code true} if this teacher created the classroom; {@code false} for co-teachers. */
    @Column(name = "is_owner", nullable = false)
    private boolean isOwner;

    /** Timestamp set automatically when the association is first persisted. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
