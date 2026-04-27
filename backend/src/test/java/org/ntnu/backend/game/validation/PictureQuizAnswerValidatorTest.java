package org.ntnu.backend.game.validation;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.MultiSelectAnswer;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PictureQuizAnswerValidatorTest {

    private PictureQuizAnswerValidator validator;

    // Options shared across tests:
    // id=1 "Ekte"       isCorrect=true   explanation="Dette er et ekte bilde."
    // id=2 "KI"         isCorrect=false  explanation="Dette er faktisk KI-generert."
    // id=3 "Manipulert" isCorrect=false  explanation="Bildet er digitalt manipulert."
    private List<Option> options;

    @BeforeMethod
    void setUp() {
        validator = new PictureQuizAnswerValidator();

        options = List.of(
                new PictureQuizOption(1, "Ekte",       "http://img.test/1.jpg", true,  "Dette er et ekte bilde."),
                new PictureQuizOption(2, "KI",          "http://img.test/1.jpg", false, "Dette er faktisk KI-generert."),
                new PictureQuizOption(3, "Manipulert",  "http://img.test/1.jpg", false, "Bildet er digitalt manipulert.")
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private MultiSelectAnswer answer(String... ids) {
        MultiSelectAnswer a = new MultiSelectAnswer();
        a.setSelectedIds(List.of(ids));
        return a;
    }

    // -------------------------------------------------------------------------
    // Correct answer
    // -------------------------------------------------------------------------

    @Test
    void correctAnswer_returnsCorrectTrue() {
        ValidationResult result = validator.validate(options, answer("1"));
        assertTrue(result.correct());
    }

    @Test
    void correctAnswer_returnsCorrectOptionIds() {
        ValidationResult result = validator.validate(options, answer("1"));
        assertEquals(List.of(1), result.correctOptionIds());
    }

    @Test
    void correctAnswer_returnsEmptyWrongSelectedIds() {
        ValidationResult result = validator.validate(options, answer("1"));
        assertTrue(result.wrongSelectedOptionIds().isEmpty());
    }

    @Test
    void correctAnswer_feedbackIsFromCorrectOption() {
        ValidationResult result = validator.validate(options, answer("1"));
        assertEquals("Dette er et ekte bilde.", result.feedback());
    }

    // -------------------------------------------------------------------------
    // Wrong answer
    // -------------------------------------------------------------------------

    @Test
    void wrongAnswer_returnsCorrectFalse() {
        ValidationResult result = validator.validate(options, answer("2"));
        assertFalse(result.correct());
    }

    @Test
    void wrongAnswer_feedbackIsFromSelectedWrongOption() {
        ValidationResult result = validator.validate(options, answer("2"));
        assertEquals("Dette er faktisk KI-generert.", result.feedback());
    }

    @Test
    void wrongAnswer_wrongSelectedIdsContainsSelectedId() {
        ValidationResult result = validator.validate(options, answer("2"));
        assertTrue(result.wrongSelectedOptionIds().contains(2));
    }

    @Test
    void wrongAnswer_correctOptionIdsStillReturned() {
        ValidationResult result = validator.validate(options, answer("2"));
        assertEquals(List.of(1), result.correctOptionIds());
    }

    @Test
    void differentWrongOption_feedbackIsFromThatOption() {
        ValidationResult result = validator.validate(options, answer("3"));
        assertEquals("Bildet er digitalt manipulert.", result.feedback());
    }

    // -------------------------------------------------------------------------
    // Multiple correct options scenario
    // -------------------------------------------------------------------------

    @Test
    void multipleCorrectOptions_allSelectedCorrectly_returnsTrue() {
        List<Option> multiCorrect = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/2.jpg", true,  "Ekte."),
                new PictureQuizOption(2, "KI",   "http://img.test/2.jpg", true,  "KI."),
                new PictureQuizOption(3, "Mani", "http://img.test/2.jpg", false, "Feil.")
        );

        ValidationResult result = validator.validate(multiCorrect, answer("1", "2"));
        assertTrue(result.correct());
    }

    @Test
    void multipleCorrectOptions_onlyOneSelected_returnsFalse() {
        List<Option> multiCorrect = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/2.jpg", true,  "Ekte."),
                new PictureQuizOption(2, "KI",   "http://img.test/2.jpg", true,  "KI."),
                new PictureQuizOption(3, "Mani", "http://img.test/2.jpg", false, "Feil.")
        );

        ValidationResult result = validator.validate(multiCorrect, answer("1"));
        assertFalse(result.correct());
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void emptySelection_returnsFalse() {
        MultiSelectAnswer empty = new MultiSelectAnswer();
        empty.setSelectedIds(List.of());
        ValidationResult result = validator.validate(options, empty);
        assertFalse(result.correct());
    }

    @Test
    void nullSelectedIds_returnsFalse() {
        MultiSelectAnswer nullAnswer = new MultiSelectAnswer();
        nullAnswer.setSelectedIds(null);
        ValidationResult result = validator.validate(options, nullAnswer);
        assertFalse(result.correct());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void unknownOptionId_throwsIllegalArgumentException() {
        validator.validate(options, answer("99"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void wrongSubmissionType_throwsIllegalArgumentException() {
        SubmitAnswerRequest wrongType = new SubmitAnswerRequest() {};
        validator.validate(options, wrongType);
    }
}