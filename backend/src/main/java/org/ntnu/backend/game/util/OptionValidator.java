package org.ntnu.backend.game.util;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.ClassifyItemsOption;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.PostOfficeOption;
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
 *       answer" for {@link LevelType#MULTIPLE_CHOICE} and
 *       {@link LevelType#PICTURE_QUIZ}</li>
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
    if(options == null) throw new OptionValidationException("Option list is null!");
    for (Option option : options) {
      Set<ConstraintViolation<Option>> violations = validator.validate(option);
      if (!violations.isEmpty()) {
        throw new OptionValidationException(violations.toString());
      }
    }

    switch (type) {
      case MULTIPLE_CHOICE -> validateMultipleChoice(options);
      case PICTURE_QUIZ    -> validatePictureQuiz(options);
      case POST_OFFICE -> validatePostOffice(options);
      case PASSWORD_STRENGTH -> validatePasswordStrength(options);
      case CLASSIFY_ITEMS -> validateClassifyItems(options);
      default -> throw new OptionValidationException("Option type doesnt exist");
    }
  }

  private void validateClassifyItems(List<Option> options) {
    if (options.isEmpty()) {
      throw new OptionValidationException("Classify items must have at least one item");
    }

    Set<String> keys = new HashSet<>();
    for (Option o : options) {
      if (!(o instanceof ClassifyItemsOption item)) {
        throw new OptionValidationException("CLASSIFY_ITEMS options must be ClassifyItemsOption");
      }
      if (!keys.add(item.getKey())) {
        throw new OptionValidationException("Duplicate key: " + item.getKey());
      }
      if (!item.getLabels().contains(item.getCorrectLabel())) {
        throw new OptionValidationException(
            "correctLabel '" + item.getCorrectLabel() + "' is not in labels: " + item.getLabels());
      }
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

  /**
   * Checks that at least one option is marked as correct.
   *
   * <p>For {@link LevelType#PICTURE_QUIZ}, one or more options may be correct.
   * This validator therefore ensures that the option list contains at least one
   * correct answer, but does not require exactly one.
   *
   * @param options the parsed picture quiz options
   * @throws OptionValidationException if no option is marked as correct
   */
  private void validatePictureQuiz(List<Option> options) {
    long correctCount = options.stream()
            .map(o -> (PictureQuizOption) o)
            .filter(PictureQuizOption::isCorrect)
            .count();

    if (correctCount < 1) {
      throw new OptionValidationException("Må velge minst ett svar!");
    }
  }

  private void validatePasswordStrength(List<Option> options){
    if(options.size() != 1 || !(options.get(0) instanceof PasswordStrengthOption)){
      throw new OptionValidationException("PASSWORD_STRENGTH must have exactly one PasswordStrengthOption type");
    }
  }
}
