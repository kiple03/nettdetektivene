package org.ntnu.backend.game.model;

import jakarta.persistence.*;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "level_order", nullable = false)
    private int order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelType levelType;

}