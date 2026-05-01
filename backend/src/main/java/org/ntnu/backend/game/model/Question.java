package org.ntnu.backend.game.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.game.dto.OptionResponse;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.util.OptionParser;


/**
 * A question within a {@link Level}. The question text is displayed to the
 * student, and the responseUrls are stored as a JSON blob in {@code optionContent}.
 *
 * <p>The JSON shape of {@code optionContent} depends on the parent level's
 * {@link LevelType}. Deserialization into typed {@link Option} subclasses is handled by
 * {@link OptionParser}.
 *
 * <p>Example {@code optionContent} for {@code MULTIPLE_CHOICE}:
 * <pre>{@code
 * [
 *   {"id": 1, "text": "...", "imageUrl": "...", "isCorrect": false, "explanation": "..."},
 *   {"id": 2, "text": "...", "imageUrl": "...", "isCorrect": true, "explanation": "..."}
 * ]
 * }</pre>
 *
 * <p>Invariant: {@code isDefault == (classroomId == null)}. Seeded questions have
 * {@code isDefault=true} and {@code classroomId=null}. Teacher-added questions have
 * {@code isDefault=false} and a non-null {@code classroomId}.
 *
 * @see Level
 * @see Option
 * @see OptionParser
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
@Getter @Setter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String questionText;

    /**
     * JSON blob storing the responseUrls for this question. The shape varies
     * by {@link LevelType} and is parsed at runtime by {@link OptionParser}.
     * Contains answer data (correct flags, explanations) that must not
     * be sent to the client, the mapper {@link GameMapper} handles this via the sealed
     * {@link OptionResponse} hierarchy.
     */
    @Column(columnDefinition = "JSON", nullable = false)
    private String optionContent;

    /** The level this question belongs to. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "level_id")
    private Level level;

    /** True for seeded/default questions; false for teacher-added questions. */
    @Column(name = "is_default", columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE", nullable = false)
    private boolean isDefault = true;

    /** Null for default questions; references the classroom for teacher-added questions. */
    @Column(name = "classroom_id", columnDefinition = "BINARY(16)")
    private UUID classroomId;

    /** Enforces the {@code isDefault == (classroomId == null)} invariant on persist/update. */
    @PrePersist
    @PreUpdate
    void validateInvariant() {
        if (isDefault != (classroomId == null)) {
            throw new IllegalStateException(
                "Question invariant violated: isDefault=" + isDefault
                + " but classroomId=" + classroomId);
        }
    }
}