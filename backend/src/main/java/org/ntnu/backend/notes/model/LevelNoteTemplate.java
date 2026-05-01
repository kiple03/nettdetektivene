package org.ntnu.backend.notes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.notes.model.enums.NoteCategory;

/**
 * A predefined note template attached to a level.
 *
 * <p>When a student completes a level, all templates for that level
 * are copied into {@link StudentLevelNote} rows for that student.
 * Templates are managed by admins and are never shown directly to students —
 * only the copies in {@link StudentLevelNote} are.
 *
 * @see StudentLevelNote
 */
@Entity
@Table(name = "level_note_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelNoteTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NoteCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}