package org.ntnu.backend.classroom.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "classroom_censor_word",
    uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "word"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClassroomCensorWord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "word", nullable = false, length = 50)
    private String word;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
