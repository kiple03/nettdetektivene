package org.ntnu.backend.notes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.ntnu.backend.notes.model.enums.NoteCategory;
import org.ntnu.backend.user.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * A preset note that has been granted to a student upon level completion.
 *
 * <p>Created by copying a {@link LevelNoteTemplate} when the student completes
 * the corresponding level. These notes are permanent and cannot be deleted
 * by the student.
 *
 * <p>The unique constraint on {@code (user_id, template_id)} ensures that
 * replaying a level never produces duplicate notes.
 *
 * @see LevelNoteTemplate
 * @see org.ntnu.backend.notes.service.LevelNoteService
 */
@Entity
@Table(
        name = "student_level_notes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_level_notes_user_template",
                columnNames = {"user_id", "template_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentLevelNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private LevelNoteTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NoteCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant grantedAt;
}