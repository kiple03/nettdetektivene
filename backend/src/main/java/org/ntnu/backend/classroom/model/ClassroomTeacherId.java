package org.ntnu.backend.classroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite primary key for the {@link ClassroomTeacher} join table,
 * uniquely identifying a teacher–classroom association.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClassroomTeacherId implements Serializable {

    /** The classroom half of the composite key. */
    @Column(name = "classroom_id")
    private UUID classroomId;

    /** The user (teacher) half of the composite key. */
    @Column(name = "user_id", length = 36)
    private UUID userId;
}
