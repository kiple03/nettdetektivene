package org.ntnu.backend.game.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.subtypes.SingleChoiceAnswer;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

/**
 * Validator for NEWS type questions.
 *
 * <p>Expected optionContent JSON shape:
 * <pre>{@code
 * {
 *   "articles": [
 *     { "id": "a1", "headline": "...", "source": "...", "body": "..." },
 *     { "id": "a2", "headline": "...", "source": "...", "body": "..." }
 *   ],
 *   "correctIds": ["a1"],
 *   "explanations": {
 *     "a1": "NRK er en troverdig kilde...",
 *     "a2": "Emosjonelt språk og ukjent kilde..."
 *   }
 * }
 * }</pre>
 *
 * <p>Expected submission: {@link SingleChoiceAnswer} containing the selected article id.
 */
@Component
@RequiredArgsConstructor
public class NewsAnswerValidator implements SubmittedAnswerValidator {

  private final ObjectMapper objectMapper;

  @Override
  public LevelType supportedType() {
    return LevelType.NEWS;
  }

  @Override
  public ValidationResult validate(String optionContent, SubmitAnswerRequest submission) {
    if (!(submission instanceof SingleChoiceAnswer choice)) {
      throw new IllegalArgumentException(
          "NEWS questions require a SingleChoiceAnswer submission, got "
              + submission.getClass().getSimpleName());
    }

    JsonNode root = parse(optionContent);
    Set<String> correctIds = readCorrectIds(root);
    String selectedId = choice.getSelectedId();

    boolean correct = correctIds.contains(selectedId);
    String feedback = root.path("explanations").path(selectedId).asText(null);

    return new ValidationResult(correct, feedback);
  }

  private JsonNode parse(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Invalid NEWS question content JSON", e);
    }
  }

  private Set<String> readCorrectIds(JsonNode root) {
    Set<String> ids = new HashSet<>();
    root.path("correctIds").forEach(node -> ids.add(node.asText()));
    return ids;
  }
}