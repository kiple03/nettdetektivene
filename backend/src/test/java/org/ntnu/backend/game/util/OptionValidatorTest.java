package org.ntnu.backend.game.util;

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
        new MultipleChoiceOption(1, "A", "/images/1", false, "Reason A"),
        new MultipleChoiceOption(2, "B", "/images/2", true, "Reason B")
    );

    assertDoesNotThrow(() ->
        validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_nullImageUrl_passes() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", null, true, "Reason")
    );

    assertDoesNotThrow(() ->
        validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_externalImageUrl_passes() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", "https://example.com/img.jpg", true, "Reason")
    );

    assertDoesNotThrow(() ->
        validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_noCorrectAnswer_throws() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", null, false, "Reason A"),
        new MultipleChoiceOption(2, "B", null, false, "Reason B")
    );

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_multipleCorrectAnswers_throws() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", null, true, "Reason A"),
        new MultipleChoiceOption(2, "B", null, true, "Reason B")
    );

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_missingExplanation_throws() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, "A", null, true, "")
    );

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_nullText_throws() {
    List<Option> options = List.of(
        new MultipleChoiceOption(1, null, "/images/1", true, "Reason")
    );

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_emptyList_throws() {
    List<Option> options = List.of();

    assertThrows(
        OptionValidationException.class,
        () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

}
