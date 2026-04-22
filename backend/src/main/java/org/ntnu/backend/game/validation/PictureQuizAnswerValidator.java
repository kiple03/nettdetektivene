package org.ntnu.backend.game.validation;

import java.util.List;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.SingleChoiceAnswer;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

/**
 * Validates student submissions for {@link LevelType#PICTURE_QUIZ} questions.
 *
 * <p>Expects a {@link SingleChoiceAnswer} where {@code selectedId} is the ID
 * of the category option the student chose (e.g. "Ekte", "KI-generert",
 * or "Manipulert"). Finds the matching {@link PictureQuizOption} and returns
 * whether it is correct along with its explanation.
 */
@Component
public class PictureQuizAnswerValidator implements SubmittedAnswerValidator {

    @Override
    public LevelType supportedType() {
        return LevelType.PICTURE_QUIZ;
    }

    /**
     * @param options    the question's parsed options including correct flags
     * @param submission must be a {@link SingleChoiceAnswer}
     * @return correct/incorrect with the chosen option's explanation
     * @throws IllegalArgumentException if submission is not a {@link SingleChoiceAnswer}
     *                                  or the selected option ID does not exist
     */
    @Override
    public ValidationResult validate(List<Option> options, SubmitAnswerRequest submission) {
        if (!(submission instanceof SingleChoiceAnswer choice)) {
            throw new IllegalArgumentException(
                    "PICTURE_QUIZ requires SingleChoiceAnswer, got "
                            + submission.getClass().getSimpleName());
        }

        return options.stream()
                .filter(o -> o instanceof PictureQuizOption pq && pq.getId() == choice.getSelectedId())
                .map(o -> (PictureQuizOption) o)
                .findFirst()
                .map(pq -> new ValidationResult(pq.isCorrect(), pq.getExplanation()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Option not found: " + choice.getSelectedId()));
    }
}