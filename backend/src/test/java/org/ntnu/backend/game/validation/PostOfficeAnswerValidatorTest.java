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
import org.ntnu.backend.game.model.PostOfficeOption;
import org.ntnu.backend.game.model.enums.LevelType;

class PostOfficeAnswerValidatorTest {

    private PostOfficeAnswerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PostOfficeAnswerValidator();
    }

    @Test
    void supportedType_shouldReturnPostOffice() {
        assertThat(validator.supportedType()).isEqualTo(LevelType.POST_OFFICE);
    }

    @Test
    void validate_whenSelectedIdsExactlyMatchSuspiciousIds_shouldReturnCorrect() {
        PostOfficeOption suspiciousOne = option("sender", true, "Sender looks suspicious.");
        PostOfficeOption safeOption = option("subject", false, "Subject is normal.");
        PostOfficeOption suspiciousTwo = option("link", true, "Link looks suspicious.");

        MultiSelectAnswer submission = submission(List.of("sender", "link"));

        ValidationResult result = validator.validate(
                List.of(suspiciousOne, safeOption, suspiciousTwo),
                submission
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback())
                .contains("Sender looks suspicious.")
                .contains("Link looks suspicious.")
                .doesNotContain("Subject is normal.");
    }

    @Test
    void validate_whenMissingSuspiciousId_shouldReturnIncorrect() {
        PostOfficeOption suspiciousOne = option("sender", true, "Sender looks suspicious.");
        PostOfficeOption safeOption = option("subject", false, "Subject is normal.");
        PostOfficeOption suspiciousTwo = option("link", true, "Link looks suspicious.");

        MultiSelectAnswer submission = submission(List.of("sender"));

        ValidationResult result = validator.validate(
                List.of(suspiciousOne, safeOption, suspiciousTwo),
                submission
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback())
                .contains("Sender looks suspicious.")
                .contains("Link looks suspicious.")
                .doesNotContain("Subject is normal.");
    }

    @Test
    void validate_whenExtraSafeIdIsSelected_shouldReturnIncorrectAndIncludeSelectedSafeFeedback() {
        PostOfficeOption suspicious = option("sender", true, "Sender looks suspicious.");
        PostOfficeOption safe = option("subject", false, "Subject is normal.");

        MultiSelectAnswer submission = submission(List.of("sender", "subject"));

        ValidationResult result = validator.validate(
                List.of(suspicious, safe),
                submission
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback())
                .contains("Sender looks suspicious.")
                .contains("Subject is normal.");
    }

    @Test
    void validate_whenNoOptionsAreSuspiciousAndNothingSelected_shouldReturnCorrectWithEmptyFeedback() {
        PostOfficeOption safeOne = option("subject", false, "Subject is normal.");
        PostOfficeOption safeTwo = option("body", false, "Body is normal.");

        MultiSelectAnswer submission = submission(List.of());

        ValidationResult result = validator.validate(
                List.of(safeOne, safeTwo),
                submission
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEmpty();
    }

    @Test
    void validate_whenNoOptionsAreSuspiciousButOneIsSelected_shouldReturnIncorrectWithSelectedFeedback() {
        PostOfficeOption safeOne = option("subject", false, "Subject is normal.");
        PostOfficeOption safeTwo = option("body", false, "Body is normal.");

        MultiSelectAnswer submission = submission(List.of("body"));

        ValidationResult result = validator.validate(
                List.of(safeOne, safeTwo),
                submission
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback())
                .contains("Body is normal.")
                .doesNotContain("Subject is normal.");
    }

    @Test
    void validate_whenSubmissionHasDuplicateSelectedIds_shouldStillCompareAsSet() {
        PostOfficeOption suspicious = option("sender", true, "Sender looks suspicious.");

        MultiSelectAnswer submission = submission(List.of("sender", "sender"));

        ValidationResult result = validator.validate(
                List.of(suspicious),
                submission
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).contains("Sender looks suspicious.");
    }

    @Test
    void validate_whenSelectedIdDoesNotExist_shouldThrowIllegalArgumentException() {
        PostOfficeOption suspicious = option("sender", true, "Sender looks suspicious.");

        MultiSelectAnswer submission = submission(List.of("unknown"));

        assertThatThrownBy(() -> validator.validate(List.of(suspicious), submission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Option not found: unknown");
    }

    @Test
    void validate_whenSubmissionIsNotMultiSelectAnswer_shouldThrowIllegalArgumentException() {
        PostOfficeOption suspicious = option("sender", true, "Sender looks suspicious.");
        SubmitAnswerRequest wrongSubmission = mock(SubmitAnswerRequest.class);

        assertThatThrownBy(() -> validator.validate(List.of(suspicious), wrongSubmission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("POST_OFFICE requires MultiChoiceAnswer");
    }

    @Test
    void validate_whenOptionsAreEmptyAndNothingSelected_shouldReturnCorrect() {
        MultiSelectAnswer submission = submission(List.of());

        ValidationResult result = validator.validate(List.of(), submission);

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEmpty();
    }

    @Test
    void validate_whenOptionsAreEmptyAndSomethingSelected_shouldThrowIllegalArgumentException() {
        MultiSelectAnswer submission = submission(List.of("sender"));

        assertThatThrownBy(() -> validator.validate(List.of(), submission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Option not found: sender");
    }

    private PostOfficeOption option(String id, boolean suspicious, String explanation) {
        PostOfficeOption option = mock(PostOfficeOption.class);
        when(option.getId()).thenReturn(id);
        when(option.isSuspicious()).thenReturn(suspicious);
        when(option.getExplanation()).thenReturn(explanation);
        return option;
    }

    private MultiSelectAnswer submission(List<String> selectedIds) {
        MultiSelectAnswer answer = mock(MultiSelectAnswer.class);
        when(answer.getSelectedIds()).thenReturn(selectedIds);
        return answer;
    }
}