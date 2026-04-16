package org.ntnu.backend.game.service;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

@Component
public class OptionValidator {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  public void validate(List<Option> options, LevelType type) {
    for (Option option : options) {
      Set<ConstraintViolation<Option>> violations = validator.validate(option);
      if (!violations.isEmpty()) {
        throw new OptionValidationException(violations.toString());
      }
    }

    switch (type) {
      case MULTIPLE_CHOICE -> validateMultipleChoice(options);
    }
  }

  private void validateMultipleChoice(List<Option> options) {
    long correctCount = options.stream()
        .map(o -> (MultipleChoiceOption) o)
        .filter(MultipleChoiceOption::getIsCorrect)
        .count();

    if (correctCount != 1) {
      throw new OptionValidationException("Multiple choice must have exactly one correct answer");
    }
  }
}
