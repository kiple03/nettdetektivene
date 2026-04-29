package org.ntnu.backend.game.validation;

import java.util.List;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.SingleChoiceAnswer;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

@Component
public class MultipleChoiceAnswerValidator implements SubmittedAnswerValidator {

  @Override
  public LevelType supportedType() {
    return LevelType.MULTIPLE_CHOICE;
  }

  /**
   * Checks if the {@link SubmitAnswerRequest} is of correct for {@link LevelType#MULTIPLE_CHOICE}
   *
   * <p>It should be a instance of {@link SingleChoiceAnswer}, and only have one correct answer</p>
   *
   * @param options the question's parsed options including correct flags
   * @param submission must be a {@link SingleChoiceAnswer}
   * @return correct/incorrect with the option's explanation
   * @throws IllegalArgumentException if submission is not a {@link SingleChoiceAnswer}
   *                                  or the selected option ID does not exist
   */
  @Override
  public ValidationResult validate(List<Option> options, SubmitAnswerRequest submission) {
    if (!(submission instanceof SingleChoiceAnswer choice)) {
      throw new IllegalArgumentException(
          "MULTIPLE_CHOICE requires SingleChoiceAnswer, got "
              + submission.getClass().getSimpleName());
    }

    List<Integer> correctOptionIds = options.stream()
            .map(o -> (MultipleChoiceOption) o)
            .filter(MultipleChoiceOption::isCorrect)
            .map(MultipleChoiceOption::getId)
            .toList();

    return options.stream()
            .filter(o -> o instanceof MultipleChoiceOption mc && mc.getId() == choice.getSelectedId())
            .map(o -> (MultipleChoiceOption) o)
            .findFirst()
            .map(mc -> new ValidationResult(
                    mc.isCorrect(),
                    mc.getExplanation(),
                    correctOptionIds.stream().map(String::valueOf).toList(),
                    List.of()
            ))
            .orElseThrow(() -> new IllegalArgumentException(
                    "Option not found: " + choice.getSelectedId()));
  }
}