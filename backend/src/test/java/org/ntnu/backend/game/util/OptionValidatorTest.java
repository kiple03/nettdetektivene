package org.ntnu.backend.game.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.enums.LevelType;

class OptionValidatorTest {

  private Validator beanValidator;
  private OptionValidator validator;

  @BeforeEach
  void setUp() {
    beanValidator = mock(Validator.class);
    validator = new OptionValidator(beanValidator);

    doReturn(Set.of()).when(beanValidator).validate(any(Option.class));
  }

  @Test
  void validate_whenOptionsIsNull_throws() {
    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(null, LevelType.MULTIPLE_CHOICE)
    );

    assertEquals("Option list is null!", exception.getMessage());
    verifyNoInteractions(beanValidator);
  }

  @Test
  void validate_whenBeanValidationFails_throws() {
    MultipleChoiceOption option = multipleChoiceOption(1, "A", true, "Reason");

    @SuppressWarnings("unchecked")
    ConstraintViolation<Option> violation = mock(ConstraintViolation.class);

    doReturn(Set.of(violation)).when(beanValidator).validate(option);

    assertThrows(
            OptionValidationException.class,
            () -> validator.validate(List.of(option), LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_validOptions_passes() {
    List<Option> options = List.of(
            multipleChoiceOption(1, "A", false, "Reason A"),
            multipleChoiceOption(2, "B", true, "Reason B")
    );

    assertDoesNotThrow(() ->
            validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );
  }

  @Test
  void validateMultipleChoice_noCorrectAnswer_throws() {
    List<Option> options = List.of(
            multipleChoiceOption(1, "A", false, "Reason A"),
            multipleChoiceOption(2, "B", false, "Reason B")
    );

    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );

    assertTrue(exception.getMessage().contains("Multiple choice must have exactly one correct answer"));
  }

  @Test
  void validateMultipleChoice_multipleCorrectAnswers_throws() {
    List<Option> options = List.of(
            multipleChoiceOption(1, "A", true, "Reason A"),
            multipleChoiceOption(2, "B", true, "Reason B")
    );

    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(options, LevelType.MULTIPLE_CHOICE)
    );

    assertTrue(exception.getMessage().contains("Multiple choice must have exactly one correct answer"));
  }

  @Test
  void validateMultipleChoice_emptyList_throws() {
    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(List.of(), LevelType.MULTIPLE_CHOICE)
    );

    assertTrue(exception.getMessage().contains("Multiple choice must have exactly one correct answer"));
  }

  @Test
  void validatePictureQuiz_withOneCorrectOption_passes() {
    List<Option> options = List.of(
            pictureQuizOption(1, true),
            pictureQuizOption(2, false)
    );

    assertDoesNotThrow(() ->
            validator.validate(options, LevelType.PICTURE_QUIZ)
    );
  }

  @Test
  void validatePictureQuiz_withMultipleCorrectOptions_passes() {
    List<Option> options = List.of(
            pictureQuizOption(1, true),
            pictureQuizOption(2, true)
    );

    assertDoesNotThrow(() ->
            validator.validate(options, LevelType.PICTURE_QUIZ)
    );
  }

  @Test
  void validatePictureQuiz_withoutCorrectOption_throws() {
    List<Option> options = List.of(
            pictureQuizOption(1, false),
            pictureQuizOption(2, false)
    );

    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(options, LevelType.PICTURE_QUIZ)
    );

    assertTrue(exception.getMessage().contains("Må velge minst ett svar!"));
  }

  @Test
  void validatePictureQuiz_emptyList_throws() {
    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(List.of(), LevelType.PICTURE_QUIZ)
    );

    assertTrue(exception.getMessage().contains("Må velge minst ett svar!"));
  }

  @Test
  void validatePostOffice_withSuspiciousOption_passes() {
    List<Option> options = List.of(
            postOfficeOption("sender", true),
            postOfficeOption("subject", false)
    );

    assertDoesNotThrow(() ->
            validator.validate(options, LevelType.POST_OFFICE)
    );
  }

  @Test
  void validatePostOffice_withMultipleSuspiciousOptions_passes() {
    List<Option> options = List.of(
            postOfficeOption("sender", true),
            postOfficeOption("link", true),
            postOfficeOption("subject", false)
    );

    assertDoesNotThrow(() ->
            validator.validate(options, LevelType.POST_OFFICE)
    );
  }

  @Test
  void validatePostOffice_withoutSuspiciousOption_throws() {
    List<Option> options = List.of(
            postOfficeOption("sender", false),
            postOfficeOption("subject", false)
    );

    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(options, LevelType.POST_OFFICE)
    );

    assertTrue(exception.getMessage().contains("Post office question must have at least one suspicious option"));
  }

  @Test
  void validatePostOffice_emptyList_throws() {
    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(List.of(), LevelType.POST_OFFICE)
    );

    assertTrue(exception.getMessage().contains("Post office question must have at least one suspicious option"));
  }

  @Test
  void validatePasswordStrength_withOnePasswordStrengthOption_passes() {
    List<Option> options = List.of(passwordStrengthOption());

    assertDoesNotThrow(() ->
            validator.validate(options, LevelType.PASSWORD_STRENGTH)
    );
  }

  @Test
  void validatePasswordStrength_emptyList_throws() {
    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(List.of(), LevelType.PASSWORD_STRENGTH)
    );

    assertTrue(exception.getMessage().contains("PASSWORD_STRENGTH must have exactly one PasswordStrengthOption type"));
  }

  @Test
  void validatePasswordStrength_withMoreThanOneOption_throws() {
    List<Option> options = List.of(
            passwordStrengthOption(),
            passwordStrengthOption()
    );

    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(options, LevelType.PASSWORD_STRENGTH)
    );

    assertTrue(exception.getMessage().contains("PASSWORD_STRENGTH must have exactly one PasswordStrengthOption type"));
  }

  @Test
  void validatePasswordStrength_withWrongOptionType_throws() {
    List<Option> options = List.of(
            multipleChoiceOption(1, "A", true, "Reason")
    );

    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> validator.validate(options, LevelType.PASSWORD_STRENGTH)
    );

    assertTrue(exception.getMessage().contains("PASSWORD_STRENGTH must have exactly one PasswordStrengthOption type"));
  }

  private MultipleChoiceOption multipleChoiceOption(
          int id,
          String text,
          boolean correct,
          String explanation
  ) {
    MultipleChoiceOption option = mock(MultipleChoiceOption.class);
    when(option.getId()).thenReturn(id);
    when(option.getText()).thenReturn(text);
    when(option.isCorrect()).thenReturn(correct);
    when(option.getExplanation()).thenReturn(explanation);
    return option;
  }

  private PictureQuizOption pictureQuizOption(int id, boolean correct) {
    PictureQuizOption option = mock(PictureQuizOption.class);
    when(option.id()).thenReturn(id);
    when(option.isCorrect()).thenReturn(correct);
    return option;
  }

  private PostOfficeOption postOfficeOption(String id, boolean suspicious) {
    PostOfficeOption option = mock(PostOfficeOption.class);
    when(option.getId()).thenReturn(id);
    when(option.isSuspicious()).thenReturn(suspicious);
    return option;
  }

  private PasswordStrengthOption passwordStrengthOption() {
    return mock(PasswordStrengthOption.class);
  }
}