package org.ntnu.backend.game.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                option(1, true, "Dette er et ekte bilde."),
                option(2, false, "Dette er faktisk KI-generert."),
                option(3, false, "Bildet er digitalt manipulert.")
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
                option(1, true, "Ekte."),
                option(2, true, "KI."),
                option(3, false, "Feil.")
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
                option(1, true, "Ekte."),
                option(2, true, "KI."),
                option(3, false, "Feil.")
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
                option(1, true, "   "),
                option(2, false, "Wrong")
        );

        ValidationResult result = validator.validate(blankExplanation, answer("1"));

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
    }

    @Test
    void validate_whenCorrectExplanationIsNull_shouldReturnNullFeedback() {
        List<Option> nullExplanation = List.of(
                option(1, true, null),
                option(2, false, "Wrong")
        );

        ValidationResult result = validator.validate(nullExplanation, answer("1"));

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isNull();
        assertThat(result.correctOptionIds()).containsExactly(1);
    }

    @Test
    void validate_whenWrongSelectedExplanationIsBlank_shouldReturnNullFeedback() {
        List<Option> blankWrongExplanation = List.of(
                option(1, true, "Correct"),
                option(2, false, "   ")
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
                option(1, true, "Correct"),
                option(2, false, null)
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
        SubmitAnswerRequest wrongSubmission = mock(SubmitAnswerRequest.class);

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

    private PictureQuizOption option(int id, boolean correct, String explanation) {
        PictureQuizOption option = mock(PictureQuizOption.class);
        when(option.id()).thenReturn(id);
        when(option.isCorrect()).thenReturn(correct);
        when(option.explanation()).thenReturn(explanation);
        return option;
    }

    private MultiSelectAnswer answer(String... ids) {
        MultiSelectAnswer answer = new MultiSelectAnswer();
        answer.setSelectedIds(List.of(ids));
        return answer;
    }
}