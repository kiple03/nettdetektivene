package org.ntnu.backend.game.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.MultiSelectAnswer;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PictureQuizOption;
import org.ntnu.backend.game.model.enums.LevelType;

class PictureQuizAnswerValidatorTest {

    private PictureQuizAnswerValidator validator;
    private List<Option> options;

    @BeforeEach
    void setUp() {
        validator = new PictureQuizAnswerValidator();

        options = List.of(
                new PictureQuizOption(
                        1,
                        "Ekte",
                        "http://img.test/1.jpg",
                        true,
                        "Dette er et ekte bilde."
                ),
                new PictureQuizOption(
                        2,
                        "KI",
                        "http://img.test/2.jpg",
                        false,
                        "Dette er faktisk KI-generert."
                ),
                new PictureQuizOption(
                        3,
                        "Manipulert",
                        "http://img.test/3.jpg",
                        false,
                        "Bildet er digitalt manipulert."
                )
        );
    }

    @Test
    void supportedType_shouldReturnPictureQuiz() {
        assertThat(validator.supportedType()).isEqualTo(LevelType.PICTURE_QUIZ);
    }

    @Test
    void validate_whenCorrectAnswerSelected_shouldReturnCorrectResult() {
        ValidationResult result = validator.validate(options, answer("1"));

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEqualTo("Dette er et ekte bilde.");
        assertThat(result.correctOptionIds()).containsExactly(1);
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenWrongAnswerSelected_shouldReturnIncorrectResult() {
        ValidationResult result = validator.validate(options, answer("2"));

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isEqualTo("Dette er faktisk KI-generert.");
        assertThat(result.correctOptionIds()).containsExactly(1);
        assertThat(result.wrongSelectedOptionIds()).containsExactly(2);
    }

    @Test
    void validate_whenDifferentWrongAnswerSelected_shouldReturnThatWrongExplanation() {
        ValidationResult result = validator.validate(options, answer("3"));

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isEqualTo("Bildet er digitalt manipulert.");
        assertThat(result.correctOptionIds()).containsExactly(1);
        assertThat(result.wrongSelectedOptionIds()).containsExactly(3);
    }

    @Test
    void validate_whenMultipleCorrectOptionsAllSelected_shouldReturnCorrect() {
        List<Option> multiCorrect = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/1.jpg", true, "Ekte."),
                new PictureQuizOption(2, "KI", "http://img.test/2.jpg", true, "KI."),
                new PictureQuizOption(3, "Manipulert", "http://img.test/3.jpg", false, "Feil.")
        );

        ValidationResult result = validator.validate(multiCorrect, answer("1", "2"));

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEqualTo("Ekte.");
        assertThat(result.correctOptionIds()).containsExactly(1, 2);
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenOnlyOneOfMultipleCorrectOptionsSelected_shouldReturnIncorrect() {
        List<Option> multiCorrect = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/1.jpg", true, "Ekte."),
                new PictureQuizOption(2, "KI", "http://img.test/2.jpg", true, "KI."),
                new PictureQuizOption(3, "Manipulert", "http://img.test/3.jpg", false, "Feil.")
        );

        ValidationResult result = validator.validate(multiCorrect, answer("1"));

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1, 2);
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenCorrectExplanationIsBlank_shouldReturnNullFeedback() {
        List<Option> blankExplanation = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/1.jpg", true, "   "),
                new PictureQuizOption(2, "KI", "http://img.test/2.jpg", false, "Wrong")
        );

        ValidationResult result = validator.validate(blankExplanation, answer("1"));

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
    }

    @Test
    void validate_whenCorrectExplanationIsNull_shouldReturnNullFeedback() {
        List<Option> nullExplanation = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/1.jpg", true, null),
                new PictureQuizOption(2, "KI", "http://img.test/2.jpg", false, "Wrong")
        );

        ValidationResult result = validator.validate(nullExplanation, answer("1"));

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
    }

    @Test
    void validate_whenWrongSelectedExplanationIsBlank_shouldReturnNullFeedback() {
        List<Option> blankWrongExplanation = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/1.jpg", true, "Correct"),
                new PictureQuizOption(2, "KI", "http://img.test/2.jpg", false, "   ")
        );

        ValidationResult result = validator.validate(blankWrongExplanation, answer("2"));

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
        assertThat(result.wrongSelectedOptionIds()).containsExactly(2);
    }

    @Test
    void validate_whenWrongSelectedExplanationIsNull_shouldReturnNullFeedback() {
        List<Option> nullWrongExplanation = List.of(
                new PictureQuizOption(1, "Ekte", "http://img.test/1.jpg", true, "Correct"),
                new PictureQuizOption(2, "KI", "http://img.test/2.jpg", false, null)
        );

        ValidationResult result = validator.validate(nullWrongExplanation, answer("2"));

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
        assertThat(result.wrongSelectedOptionIds()).containsExactly(2);
    }

    @Test
    void validate_whenSelectionIsEmpty_shouldReturnIncorrect() {
        ValidationResult result = validator.validate(options, answer());

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenSelectedIdsIsNull_shouldTreatAsEmptySelection() {
        MultiSelectAnswer submission = new MultiSelectAnswer();
        submission.setSelectedIds(null);

        ValidationResult result = validator.validate(options, submission);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenSelectedIdDoesNotExist_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> validator.validate(options, answer("99")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Option not found 99");
    }

    @Test
    void validate_whenSelectedIdIsNotANumber_shouldThrowNumberFormatException() {
        assertThatThrownBy(() -> validator.validate(options, answer("abc")))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void validate_whenSubmissionIsNotMultiSelectAnswer_shouldThrowIllegalArgumentException() {
        SubmitAnswerRequest wrongSubmission = new SubmitAnswerRequest() {};

        assertThatThrownBy(() -> validator.validate(options, wrongSubmission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PICTURE_QUIZ requires MultiSelectAnswer");
    }

    @Test
    void validate_whenOptionsAreEmptyAndNothingSelected_shouldReturnCorrect() {
        ValidationResult result = validator.validate(List.of(), answer());

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).isEmpty();
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenOptionsAreEmptyAndSomethingSelected_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> validator.validate(List.of(), answer("1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Option not found 1");
    }

    private MultiSelectAnswer answer(String... ids) {
        MultiSelectAnswer answer = new MultiSelectAnswer();
        answer.setSelectedIds(List.of(ids));
        return answer;
    }
}