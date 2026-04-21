package org.ntnu.backend.classroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

/**
 * Combined primary key used in ClassroomTeacher. 
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClassroomTeacherId implements Serializable {

    @Column(name = "classroom_id")
    private UUID classroomId;

    @Column(name = "user_id", length = 36)
    private UUID userId;
}
