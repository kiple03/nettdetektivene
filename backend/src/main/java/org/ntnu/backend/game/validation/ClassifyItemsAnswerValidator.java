package org.ntnu.backend.game.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.PerItemAnswer;
import org.ntnu.backend.game.model.ClassifyItemsOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

@Component
public class ClassifyItemsAnswerValidator implements SubmittedAnswerValidator {

  @Override
  public LevelType supportedType() {
    return LevelType.CLASSIFY_ITEMS;
  }

  @Override
  public ValidationResult validate(List<Option> options, SubmitAnswerRequest submission) {
    if (!(submission instanceof PerItemAnswer perItem)) {
      throw new IllegalArgumentException(
          "CLASSIFY_ITEMS requires PerItemAnswer, got " + submission.getClass().getSimpleName());
    }

    Map<String, ClassifyItemsOption> byKey = options.stream()
        .map(o -> (ClassifyItemsOption) o)
        .collect(Collectors.toMap(ClassifyItemsOption::getKey, Function.identity()));

    List<String> wrongFeedback = new ArrayList<>();

    for (Map.Entry<String, String> entry : perItem.getAnswers().entrySet()) {
      ClassifyItemsOption item = byKey.get(entry.getKey());
      if (item == null) {
        throw new IllegalArgumentException("Unknown key: " + entry.getKey());
      }
      if (!item.getCorrectLabel().equals(entry.getValue())) {
        wrongFeedback.add(item.getExplanation());
      }
    }

    if (perItem.getAnswers().size() != options.size()) {
      throw new IllegalArgumentException(
          "Expected " + options.size() + " answers, got " + perItem.getAnswers().size());
    }

    boolean correct = wrongFeedback.isEmpty();
    String feedback = correct
        ? "Riktig! Du klassifiserte alt rett!"
        : String.join(" | ", wrongFeedback);

    return new ValidationResult(correct, feedback, List.of(), List.of());
  }
}