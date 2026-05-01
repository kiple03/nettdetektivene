package org.ntnu.backend.classroom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A word that is censored/blocked within a specific classroom.
 * Each classroom-word pair is unique — the same word cannot be added twice to the same classroom.
 */
@Entity
@Table(
    name = "classroom_censor_word",
    uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "word"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClassroomCensorWord {

    /** Unique identifier for this censor-word entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /** The classroom this censored word belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /** The word to be censored; max 50 characters. */
    @Column(name = "word", nullable = false, length = 50)
    private String word;

    /** Timestamp of when this censor word was added. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
