package org.ntnu.backend.classroom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a classroom created by a teacher.
 * Students join a classroom using its unique {@link #joinCode}.
 */
@Entity
@Table(name = "classroom")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Classroom {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /** Display name of the classroom (max 200 chars). */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Optional longer description of the classroom. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Short unique code that students enter to join this classroom. */
    @Column(name = "join_code", unique = true, nullable = false, length = 20)
    private String joinCode;

    /** Timestamp set automatically when the classroom is first persisted. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}