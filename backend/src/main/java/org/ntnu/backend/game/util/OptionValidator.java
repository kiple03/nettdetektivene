package org.ntnu.backend.game.util;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
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
      case PASSWORD_STRENGTH -> validatePasswordStrength(options);
      default -> {/* No rules */}
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

  private void validatePasswordStrength(List<Option> options){
    if(options.size() != 1 || !(options.get(0) instanceof PasswordStrengthOption)){
      throw new OptionValidationException("PASSWORD_STRENGTH must have exactly one PasswordStrengthOption type");
    }
  }
}
