package org.ntnu.backend.game.validation;

import java.util.List;
import org.ntnu.backend.game.dto.optionresponse.OptionImageResponse;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtype.SingleChoiceAnswer;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

/** Validates answers submitted for multiple-choice questions (single correct option). */
@Component
public class MultipleChoiceAnswerValidator implements SubmittedAnswerValidator {

  /** @return {@link LevelType#MULTIPLE_CHOICE} */
  @Override
  public LevelType supportedType() {
    return LevelType.MULTIPLE_CHOICE;
  }

  /**
   * Checks if the {@link SubmitAnswerRequest} is of correct for {@link LevelType#MULTIPLE_CHOICE}
   *
   * <p>It should be a instance of {@link SingleChoiceAnswer}, and only have one correct answer</p>
   *
   * @param options the question's parsed responseUrls including correct flags
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

    List<MultipleChoiceOption> mcOptions = options.stream()
        .map(o -> (MultipleChoiceOption) o)
        .toList();

    List<Integer> correctOptionIds = mcOptions.stream()
        .filter(MultipleChoiceOption::isCorrect)
        .map(MultipleChoiceOption::getId)
        .toList();

    List<OptionImageResponse> responseImages = mcOptions.stream()
        .map(mc -> new OptionImageResponse(mc.getId(), mc.getResponseImageUrl()))
        .toList();

    return mcOptions.stream()
        .filter(mc -> mc.getId() == choice.getSelectedId())
        .findFirst()
        .map(mc -> new ValidationResult(
            mc.isCorrect(),
            mc.getExplanation(),  // always return explanation, not null
            correctOptionIds.stream().map(String::valueOf).toList(),
            mc.isCorrect() ? List.of() : List.of(String.valueOf(mc.getId())),
            responseImages
        ))
        .orElseThrow(() -> new IllegalArgumentException(
            "Option not found: " + choice.getSelectedId()));
  }
}