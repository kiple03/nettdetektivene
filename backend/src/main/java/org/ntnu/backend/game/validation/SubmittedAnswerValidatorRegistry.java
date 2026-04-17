package org.ntnu.backend.game.validation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

/**
 * Looks up the {@link SubmittedAnswerValidator} for a given {@link LevelType}.
 *
 * <p>Spring injects every {@code SubmittedAnswerValidator} bean in the
 * application context into the constructor, and this registry indexes them
 * by their supported type.
 */
@Component
public class SubmittedAnswerValidatorRegistry {

  private final Map<LevelType, SubmittedAnswerValidator> validators;

  public SubmittedAnswerValidatorRegistry(List<SubmittedAnswerValidator> all) {
    this.validators = all.stream()
        .collect(Collectors.toMap(SubmittedAnswerValidator::supportedType, v -> v));
  }

  /**
   * Returns the validator for the given level type.
   *
   * @throws IllegalStateException if no validator is registered for the type
   */
  public SubmittedAnswerValidator forType(LevelType type) {
    SubmittedAnswerValidator validator = validators.get(type);
    if (validator == null) {
      throw new IllegalStateException("No validator registered for level type: " + type);
    }
    return validator;
  }
}