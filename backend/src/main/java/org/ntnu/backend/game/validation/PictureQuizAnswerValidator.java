package org.ntnu.backend.game.validation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.MultiSelectAnswer;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

/**
 * Validates student submissions for {@link LevelType#PICTURE_QUIZ} questions.
 *
 * <p>Expects a {@link MultiSelectAnswer} where {@code selectedIds} contains
 * the IDs of all responseUrls the student selected. A submission is considered
 * correct only if:
 *
 * <ul>
 *   <li>all selected option IDs are correct, and</li>
 *   <li>all correct option IDs are included in the submission.</li>
 * </ul>
 *
 * <p>In other words, the selected option IDs must exactly match the set of
 * correct option IDs. The validator returns whether the submission is correct,
 * along with an explanation taken from one of the correct
 * {@link PictureQuizOption} entries.
 */
@Component
public class PictureQuizAnswerValidator implements SubmittedAnswerValidator {

    /** @return {@link LevelType#PICTURE_QUIZ} */
    @Override
    public LevelType supportedType() {
        return LevelType.PICTURE_QUIZ;
    }

    /**
     * Validates a submitted answer for a picture quiz question.
     *
     * @param options the question's parsed responseUrls including correct flags
     * @param submission must be a {@link MultiSelectAnswer}
     * @return a {@link ValidationResult} indicating whether the selected option IDs
     *         exactly match the correct option IDs, along with an explanation from
     *         one of the correct responseUrls
     * @throws IllegalArgumentException if submission is not a {@link MultiSelectAnswer}
     *                                  or if any selected option ID does not exist
     */
    @Override
    public ValidationResult validate(List<Option> options, SubmitAnswerRequest submission) {
        if (!(submission instanceof MultiSelectAnswer choice)) {
            throw new IllegalArgumentException(
                    "PICTURE_QUIZ requires MultiSelectAnswer, got "
                            + submission.getClass().getSimpleName());
        }

        Set<Integer> selectedIds = choice.getSelectedIds() == null
            ? Set.of()
            : choice.getSelectedIds().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        List<PictureQuizOption> pq = options.stream()
                .map(o -> (PictureQuizOption) o)
                .toList();

        Set<Integer> validOptionIds = pq.stream()
            .map(PictureQuizOption::id)
            .collect(Collectors.toSet());

        for (Integer selectedId : selectedIds) {
            if (!validOptionIds.contains(selectedId)) {
                throw new IllegalArgumentException("Option not found " + selectedId);
            }
        }

        List<Integer> correctOptionIds = pq.stream()
                .filter(PictureQuizOption::isCorrect)
                .map(PictureQuizOption::id)
                .toList();

        Set<Integer> correctIdSet = Set.copyOf(correctOptionIds);

        List<Integer> wrongSelectedOptionIds = selectedIds.stream()
                .filter(selectedId -> !correctIdSet.contains(selectedId))
                .toList();

        boolean isCorrect = selectedIds.equals(correctIdSet);

        String explanation = isCorrect
                ? pq.stream()
                  .filter(PictureQuizOption::isCorrect)
                  .map(PictureQuizOption::explanation)
                  .filter(explanationText -> explanationText != null && !explanationText.isBlank())
                  .findFirst()
                  .orElse(null)
                : pq.stream()
                  .filter(option -> selectedIds.contains(option.id()))
                  .filter(option -> !option.isCorrect())
                  .map(PictureQuizOption::explanation)
                  .filter(explanationText -> explanationText != null && !explanationText.isBlank())
                  .findFirst()
                  .orElse(null);

        return new ValidationResult(
            isCorrect,
            explanation,
            correctOptionIds.stream().map(String::valueOf).toList(),
            wrongSelectedOptionIds.stream().map(String::valueOf).toList(),
            List.of()
        );
    }
}