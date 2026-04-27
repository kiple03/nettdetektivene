package org.ntnu.backend.game.util;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

/**
 * Validates parsed {@link Option} lists for data integrity.
 *
 * <p>Runs two layers of checks:
 * <ol>
 *   <li>Bean Validation: checks field annotations like {@code @NotNull}
 *       and {@code @NotBlank} on each option</li>
 *   <li>Business rules: type-specific checks like "exactly one correct
 *       answer" for {@link  LevelType#MULTIPLE_CHOICE}</li>
 * </ol>
 *
 * <p>Called by {@link OptionParser} after deserialization, and should be used when the
 *
 *
 * @see OptionParser
 * @see Option
 */
@Component
@RequiredArgsConstructor
public class OptionValidator {

  private final Validator validator;

  /**
   * Validates all options in the list — field constraints first,
   * then type-specific business rules.
   *
   * @param options the parsed options to validate
   * @param type the level type, determines which business rules apply
   * @throws OptionValidationException if any field constraint or business rule fails
   */
  public void validate(List<Option> options, LevelType type) {
    for (Option option : options) {
      Set<ConstraintViolation<Option>> violations = validator.validate(option);
      if (!violations.isEmpty()) {
        throw new OptionValidationException(violations.toString());
      }
    }

    switch (type) {
      case MULTIPLE_CHOICE -> validateMultipleChoice(options);
      case POST_OFFICE -> validatePostOffice(options);
      default -> throw new OptionValidationException("Option type doesnt exist");
    }
  }
  /**
   * Checks that exactly one option is marked as correct.
   *
   * @param options the parsed multiple choice options
   * @throws OptionValidationException if zero or more than one option is correct
   */
  private void validateMultipleChoice(List<Option> options) {
    long correctCount = options.stream()
        .map(o -> (MultipleChoiceOption) o)
        .filter(MultipleChoiceOption::isCorrect)
        .count();

    if (correctCount != 1) {
      throw new OptionValidationException("Multiple choice must have exactly one correct answer");
    }
  }

  /**
   * Checks that at least one option is marked as suspicious, and that
   * every ID listed in correctSuspicious matches an actual option ID.
   *
   * @param options the parsed post office options
   * @throws OptionValidationException if no option is suspicious
   */
  private void validatePostOffice(List<Option> options) {
    long suspiciousCount = options.stream()
        .map(o -> (PostOfficeOption) o)
        .filter(PostOfficeOption::isSuspicious)
        .count();
    if (suspiciousCount < 1) {
      throw new OptionValidationException("Post office question must have at least one suspicious option");
    }
  }
}
