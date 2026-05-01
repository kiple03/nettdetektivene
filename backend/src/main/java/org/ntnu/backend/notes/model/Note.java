package org.ntnu.backend.notes.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.ntnu.backend.notes.model.enums.NoteCategory;
import org.ntnu.backend.user.model.User;

/**
 * A personal note written by a student in their own notebook.
 *
 * <p>Unlike {@link StudentLevelNote}, which is granted automatically from a
 * {@link LevelNoteTemplate} when a level is completed, these notes are created
 * manually by the student and can be edited or deleted by their owner.
 *
 * <p>Each note belongs to a single user and is stored with a category so it can
 * be grouped into the correct notebook tab in the frontend.
 *
 * @see StudentLevelNote
 * @see org.ntnu.backend.notes.service.NoteService
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notes")

public class Note {

    /** Primary key, auto-generated UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The student who owns this note. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Notebook tab this note belongs to (e.g. CLUE, GENERAL). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NoteCategory category;

    /** Free-text body of the note. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** When the note was first created; set automatically, never updated. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** When the note was last modified; updated automatically on every save. */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}