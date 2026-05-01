package org.ntnu.backend.classroom.model;

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
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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
