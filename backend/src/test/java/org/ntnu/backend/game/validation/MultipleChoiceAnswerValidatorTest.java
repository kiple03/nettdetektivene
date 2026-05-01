package org.ntnu.backend.game.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtype.SingleChoiceAnswer;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.enums.LevelType;

class MultipleChoiceAnswerValidatorTest {

    private MultipleChoiceAnswerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MultipleChoiceAnswerValidator();
    }

    @Test
    void supportedType_shouldReturnMultipleChoice() {
        assertThat(validator.supportedType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
    }

    @Test
    void validate_whenSelectedOptionIsCorrect_shouldReturnCorrectResult() {
        MultipleChoiceOption correctOption = option(1, true, "Correct explanation");
        MultipleChoiceOption wrongOption = option(2, false, "Wrong explanation");

        SingleChoiceAnswer submission = mock(SingleChoiceAnswer.class);
        when(submission.getSelectedId()).thenReturn(1);

        ValidationResult result = validator.validate(
                List.of(correctOption, wrongOption),
                submission
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEqualTo("Correct explanation");
        assertThat(result.correctOptionIds()).containsExactly("1");
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenSelectedOptionIsIncorrect_shouldReturnIncorrectResultWithCorrectOptionIds() {
        MultipleChoiceOption correctOption = option(1, true, "Correct explanation");
        MultipleChoiceOption wrongOption = option(2, false, "This was wrong");

        SingleChoiceAnswer submission = mock(SingleChoiceAnswer.class);
        when(submission.getSelectedId()).thenReturn(2);

        ValidationResult result = validator.validate(
                List.of(correctOption, wrongOption),
                submission
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isEqualTo("This was wrong");
        assertThat(result.correctOptionIds()).containsExactly("1");
        assertThat(result.wrongSelectedOptionIds()).containsExactly("2");
    }

    @Test
    void validate_whenMultipleOptionsAreCorrect_shouldReturnAllCorrectOptionIds() {
        MultipleChoiceOption correctOptionOne = option(1, true, "Correct one");
        MultipleChoiceOption correctOptionTwo = option(2, true, "Correct two");
        MultipleChoiceOption wrongOption = option(3, false, "Wrong");

        SingleChoiceAnswer submission = mock(SingleChoiceAnswer.class);
        when(submission.getSelectedId()).thenReturn(2);

        ValidationResult result = validator.validate(
                List.of(correctOptionOne, correctOptionTwo, wrongOption),
                submission
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEqualTo("Correct two");
        assertThat(result.correctOptionIds()).containsExactly("1", "2");
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenSubmissionIsNotSingleChoiceAnswer_shouldThrowIllegalArgumentException() {
        MultipleChoiceOption option = option(1, true, "Correct explanation");

        SubmitAnswerRequest wrongSubmissionType = mock(SubmitAnswerRequest.class);

        assertThatThrownBy(() -> validator.validate(
                List.of(option),
                wrongSubmissionType
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MULTIPLE_CHOICE requires SingleChoiceAnswer");
    }

    @Test
    void validate_whenSelectedOptionDoesNotExist_shouldThrowIllegalArgumentException() {
        MultipleChoiceOption optionOne = option(1, true, "Correct explanation");
        MultipleChoiceOption optionTwo = option(2, false, "Wrong explanation");

        SingleChoiceAnswer submission = mock(SingleChoiceAnswer.class);
        when(submission.getSelectedId()).thenReturn(99);

        assertThatThrownBy(() -> validator.validate(
                List.of(optionOne, optionTwo),
                submission
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Option not found: 99");
    }

    @Test
    void validate_whenOptionsAreEmpty_shouldThrowIllegalArgumentException() {
        SingleChoiceAnswer submission = mock(SingleChoiceAnswer.class);
        when(submission.getSelectedId()).thenReturn(1);

        assertThatThrownBy(() -> validator.validate(
                List.of(),
                submission
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Option not found: 1");
    }

    private MultipleChoiceOption option(int id, boolean correct, String explanation) {
        MultipleChoiceOption option = mock(MultipleChoiceOption.class);
        when(option.getId()).thenReturn(id);
        when(option.isCorrect()).thenReturn(correct);
        when(option.getExplanation()).thenReturn(explanation);
        return option;
    }
}