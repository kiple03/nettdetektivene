package org.ntnu.backend.game.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.MultiSelectAnswer;
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

@Component
public class PostOfficeAnswerValidator implements SubmittedAnswerValidator {

  @Override
  public LevelType supportedType() {
    return LevelType.POST_OFFICE;
  }

  /**
   * Checks if the {@link SubmitAnswerRequest} is correct for {@link LevelType#POST_OFFICE}.
   *
   * <p>It should be an instance of {@link MultiSelectAnswer}, containing the IDs of all
   * elements the student believes are suspicious. The submission is correct only if the
   * selected IDs exactly match the set of responseUrls marked {@code isSuspicious = true}.</p>
   *
   * @param options  the question's parsed responseUrls including suspicious flags
   * @param submission must be a {@link MultiSelectAnswer}
   * @return correct/incorrect with aggregated feedback from all suspicious responseUrls
   * @throws IllegalArgumentException if submission is not a {@link MultiSelectAnswer}
   *                                  or any selected ID does not exist among the responseUrls
   */
  @Override
  public ValidationResult validate(List<Option> options, SubmitAnswerRequest submission) {
    if (!(submission instanceof MultiSelectAnswer choice)) {
      throw new IllegalArgumentException(
          "POST_OFFICE requires MultiChoiceAnswer, got "
              + submission.getClass().getSimpleName());
    }

    List<PostOfficeOption> postOfficeOptions = options.stream()
        .filter(o -> o instanceof PostOfficeOption)
        .map(o -> (PostOfficeOption) o)
        .toList();

    Set<String> validIds = postOfficeOptions.stream()
        .map(PostOfficeOption::getId)
        .collect(Collectors.toSet());

    choice.getSelectedIds().forEach(id -> {
      if (!validIds.contains(id)) {
        throw new IllegalArgumentException("Option not found: " + id);
      }
    });

    Set<String> correctIds = postOfficeOptions.stream()
        .filter(PostOfficeOption::isSuspicious)
        .map(PostOfficeOption::getId)
        .collect(Collectors.toSet());

    Set<String> submittedIds = Set.copyOf(choice.getSelectedIds());
    boolean isCorrect = submittedIds.equals(correctIds);

    String feedback = postOfficeOptions.stream()
        .filter(o -> submittedIds.contains(o.getId()) || o.isSuspicious())
        .map(PostOfficeOption::getExplanation)
        .collect(Collectors.joining(" "));

    List<String> correctIdList = new ArrayList<>(correctIds);
    List<String> wrongSelectedList = submittedIds.stream()
        .filter(id -> !correctIds.contains(id))
        .toList();

    return new ValidationResult(isCorrect, feedback, correctIdList, wrongSelectedList, List.of());

  }
}