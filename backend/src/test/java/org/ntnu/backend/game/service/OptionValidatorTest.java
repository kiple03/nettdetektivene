package org.ntnu.backend.game.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;

public class OptionValidatorTest {

  private final Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
  private final OptionValidator validator = new OptionValidator(beanValidator);

  @Test
  void validateMultipleChoice_validOptions_passes() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", "https://example.com/1.jpg", false, "Reason A"),
        new MultipleChoiceOption(2, "B", "https://example.com/b.jpg", true, "Reason B")
    );

    assertDoesNotThrow(() ->
        validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_noCorrectAnswer_throws() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", "", false, "Reason A"),
        new MultipleChoiceOption(2, "B", "", false, "Reason B")
    );

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_multipleCorrectAnswers_throws() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", "", true, "Reason A"),
        new MultipleChoiceOption(2, "B", "", true, "Reason B")
    );

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_missingExplanation_throws() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", "", true, "")
    );

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }
}
