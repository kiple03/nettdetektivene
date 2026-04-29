package org.ntnu.backend.game.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.exception.InvalidOptionFormatException;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.enums.LevelType;

import tools.jackson.databind.ObjectMapper;

class OptionParserTest {

  private OptionValidator optionValidator;
  private OptionParser parser;

  @BeforeEach
  void setUp() {
    optionValidator = mock(OptionValidator.class);
    parser = new OptionParser(new ObjectMapper(), optionValidator);
  }

  @Test
  void parseMultipleChoice_validJson_returnsListOfOptionsAndValidates() {
    String json = """
        [
          {
            "id": 1,
            "text": "A",
            "imageUrl": "https://example.com/a.jpg",
            "isCorrect": false,
            "explanation": "Reason A"
          },
          {
            "id": 2,
            "text": "B",
            "imageUrl": "https://example.com/b.jpg",
            "isCorrect": true,
            "explanation": "Reason B"
          }
        ]
        """;

    List<Option> result = parser.parse(json, LevelType.MULTIPLE_CHOICE);

    assertEquals(2, result.size());
    assertInstanceOf(MultipleChoiceOption.class, result.getFirst());

    MultipleChoiceOption first = (MultipleChoiceOption) result.getFirst();
    assertEquals(1, first.getId());
    assertEquals("A", first.getText());
    assertEquals("https://example.com/a.jpg", first.getImageUrl());
    assertFalse(first.isCorrect());
    assertEquals("Reason A", first.getExplanation());

    verify(optionValidator).validate(result, LevelType.MULTIPLE_CHOICE);
  }

  @Test
  void parseMultipleChoice_invalidJson_throwsInvalidOptionFormatException() {
    String json = """
        [
          { "id": "wrong-type", "text": "A" }
        ]
        """;

    assertThrows(
            InvalidOptionFormatException.class,
            () -> parser.parse(json, LevelType.MULTIPLE_CHOICE)
    );

    verifyNoInteractions(optionValidator);
  }

  @Test
  void parseMultipleChoice_whenValidatorThrows_shouldPropagateOptionValidationException() {
    String json = """
        [
          {
            "id": 1,
            "text": "A",
            "imageUrl": null,
            "isCorrect": false,
            "explanation": "Reason A"
          }
        ]
        """;

    doThrow(new OptionValidationException("Multiple choice must have exactly one correct answer"))
            .when(optionValidator)
            .validate(anyList(), eq(LevelType.MULTIPLE_CHOICE));

    OptionValidationException exception = assertThrows(
            OptionValidationException.class,
            () -> parser.parse(json, LevelType.MULTIPLE_CHOICE)
    );

    assertTrue(exception.getMessage().contains("Multiple choice must have exactly one correct answer"));
  }

  @Test
  void parsePictureQuiz_validJson_returnsPictureQuizOptionsAndValidates() {
    String json = """
        [
          {
            "id": 1,
            "text": "AI-generated",
            "imageUrl": "https://example.com/image-1.jpg",
            "isCorrect": true,
            "explanation": "This image has AI artifacts."
          },
          {
            "id": 2,
            "text": "Real",
            "imageUrl": "https://example.com/image-2.jpg",
            "isCorrect": false,
            "explanation": "This is not the right category."
          }
        ]
        """;

    List<Option> result = parser.parse(json, LevelType.PICTURE_QUIZ);

    assertEquals(2, result.size());
    assertInstanceOf(PictureQuizOption.class, result.getFirst());

    PictureQuizOption first = (PictureQuizOption) result.getFirst();
    assertEquals(1, first.id());
    assertEquals("AI-generated", first.text());
    assertEquals("https://example.com/image-1.jpg", first.imageUrl());
    assertTrue(first.isCorrect());
    assertEquals("This image has AI artifacts.", first.explanation());

    verify(optionValidator).validate(result, LevelType.PICTURE_QUIZ);
  }

  @Test
  void parsePictureQuiz_invalidJson_throwsInvalidOptionFormatException() {
    String json = """
        [
          { "id": "wrong-type", "text": "AI-generated" }
        ]
        """;

    assertThrows(
            InvalidOptionFormatException.class,
            () -> parser.parse(json, LevelType.PICTURE_QUIZ)
    );

    verifyNoInteractions(optionValidator);
  }

  @Test
  void parsePasswordStrength_validJson_returnsPasswordStrengthOptionsAndValidates() {
    String json = """
        [
          {
            "id": 1,
            "minLength": 8,
            "requireDigit": true,
            "requireUpper": true,
            "requireLower": true,
            "requireSymbol": true,
            "successFeedback": "Strong password",
            "failureFeedback": "Weak password"
          }
        ]
        """;

    List<Option> result = parser.parse(json, LevelType.PASSWORD_STRENGTH);

    assertEquals(1, result.size());
    assertInstanceOf(PasswordStrengthOption.class, result.getFirst());

    PasswordStrengthOption first = (PasswordStrengthOption) result.getFirst();
    assertEquals(1, first.getId());
    assertEquals(8, first.getMinLength());
    assertTrue(first.isRequireDigit());
    assertTrue(first.isRequireUpper());
    assertTrue(first.isRequireLower());
    assertTrue(first.isRequireSymbol());
    assertEquals("Strong password", first.getSuccessFeedback());
    assertEquals("Weak password", first.getFailureFeedback());

    verify(optionValidator).validate(result, LevelType.PASSWORD_STRENGTH);
  }

  @Test
  void parsePasswordStrength_invalidJson_throwsInvalidOptionFormatException() {
    String json = """
        [
          { "id": 1, "minLength": "eight" }
        ]
        """;

    assertThrows(
            InvalidOptionFormatException.class,
            () -> parser.parse(json, LevelType.PASSWORD_STRENGTH)
    );

    verifyNoInteractions(optionValidator);
  }

  @Test
  void parseUnsupportedLevelType_textQuiz_callsValidatorWithNullAndReturnsNull() {
    List<Option> result = parser.parse("[]", LevelType.TEXT_QUIZ);

    assertTrue(result.isEmpty());
    verify(optionValidator).validate(result, LevelType.TEXT_QUIZ);
  }

  @Test
  void parseUnsupportedLevelType_minigame_callsValidatorWithNullAndReturnsNull() {
    List<Option> result = parser.parse("[]", LevelType.MINIGAME);

    assertNull(result);
    verify(optionValidator).validate(null, LevelType.MINIGAME);
  }

  @Test
  void parseUnsupportedLevelType_bossBattle_callsValidatorWithNullAndReturnsNull() {
    List<Option> result = parser.parse("[]", LevelType.BOSS_BATTLE);

    assertNull(result);
    verify(optionValidator).validate(null, LevelType.BOSS_BATTLE);
  }

  @Test
  void parseUnsupportedLevelType_tutorial_callsValidatorWithNullAndReturnsNull() {
    List<Option> result = parser.parse("[]", LevelType.TUTORIAL);

    assertNull(result);
    verify(optionValidator).validate(null, LevelType.TUTORIAL);
  }

  @Test
  void parseUnsupportedLevelType_classifyItems_callsValidatorWithEmptyListAndReturnsEmptyList() {
    List<Option> result = parser.parse("[]", LevelType.CLASSIFY_ITEMS);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(optionValidator).validate(result, LevelType.CLASSIFY_ITEMS);
  }
}