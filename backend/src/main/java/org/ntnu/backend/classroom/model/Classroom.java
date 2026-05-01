package org.ntnu.backend.classroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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