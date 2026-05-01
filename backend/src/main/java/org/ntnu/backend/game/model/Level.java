package org.ntnu.backend.game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.validation.SubmittedAnswerValidator;

/**
 * A level within a {@link Stop}. Each stop has multiple levels with
 * increasing difficulty that students complete in sequence.
 *
 * <p>The {@code levelType} determines the format of questions within
 * this level, for example {@link LevelType#MULTIPLE_CHOICE} means each question
 * has a list of {@link MultipleChoiceOption}s. The type also controls
 * which {@link SubmittedAnswerValidator}
 * handles answer submissions.</p>
 *
 * <p>Questions are fetched via {@code QuestionRepository.findByLevelId()},
 * not stored as a collection on this entity.</p>
 *
 * @see Stop
 * @see Question
 * @see LevelType
 */
@Entity
@Table(name = "levels")
@Getter @Setter
public class  Level {

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Display title shown to students. */
    @Column(nullable = false)
    private String title;

    /** Optional introductory text for the level. */
    private String description;

    /** 1-based position within the parent stop. */
    @Column(name = "level_order", nullable = false)
    private int order;

    /** The stop this level belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    /** Question format used in this level, controls which validator handles submissions. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelType levelType;
}