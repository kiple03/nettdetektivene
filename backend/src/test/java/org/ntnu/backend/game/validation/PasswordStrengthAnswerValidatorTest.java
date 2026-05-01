package org.ntnu.backend.game.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.PasswordStrengthAnswer;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.enums.LevelType;

class PasswordStrengthAnswerValidatorTest {

    private PasswordStrengthAnswerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordStrengthAnswerValidator();
    }

    @Test
    void validate_whenAllRulesAreDisabledAndPasswordIsEmpty_shouldReturnCorrect() {
        PasswordStrengthOption rule = rule(
                0,
                false,
                false,
                false,
                false,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEqualTo("Sterkt passord");
        assertThat(result.correctOptionIds()).isEmpty();
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void supportedType_shouldReturnPasswordStrength() {
        assertThat(validator.supportedType()).isEqualTo(LevelType.PASSWORD_STRENGTH);
    }

    @Test
    void validate_whenPasswordMeetsAllRules_shouldReturnCorrectResult() {
        PasswordStrengthOption rule = rule(
                8,
                true,
                true,
                true,
                true,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("Strong1!");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEqualTo("Sterkt passord");
        assertThat(result.correctOptionIds()).isEmpty();
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenPasswordIsTooShort_shouldReturnIncorrectWithMissingMinLength() {
        PasswordStrengthOption rule = rule(
                8,
                false,
                false,
                false,
                false,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("abc");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("Svakt passord");
        assertThat(result.feedback()).contains("Mangler:");
        assertThat(result.feedback()).contains("Minst 8 tegn");
        assertThat(result.correctOptionIds()).isEmpty();
        assertThat(result.wrongSelectedOptionIds()).isEmpty();
    }

    @Test
    void validate_whenPasswordMissesDigit_shouldReturnIncorrectWithMissingDigit() {
        PasswordStrengthOption rule = rule(
                6,
                true,
                false,
                false,
                false,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("abcdef");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("Minst ett tall");
    }

    @Test
    void validate_whenPasswordMissesUppercase_shouldReturnIncorrectWithMissingUppercase() {
        PasswordStrengthOption rule = rule(
                6,
                false,
                true,
                false,
                false,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("abcdef");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("Minst èn stor bokstav");
    }

    @Test
    void validate_whenPasswordMissesLowercase_shouldReturnIncorrectWithMissingLowercase() {
        PasswordStrengthOption rule = rule(
                6,
                false,
                false,
                true,
                false,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("ABCDEF");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("Minst èn liten bokstav");
    }

    @Test
    void validate_whenPasswordMissesSymbol_shouldReturnIncorrectWithMissingSymbol() {
        PasswordStrengthOption rule = rule(
                6,
                false,
                false,
                false,
                true,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("Abcdef1");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("Minst ett symbol");
    }

    @Test
    void validate_whenPasswordMissesSeveralRules_shouldReturnAllMissingRules() {
        PasswordStrengthOption rule = rule(
                10,
                true,
                true,
                true,
                true,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer("abc");

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("Svakt passord");
        assertThat(result.feedback()).contains("Mangler:");
        assertThat(result.feedback()).contains("Minst 10 tegn");
        assertThat(result.feedback()).contains("Minst ett tall");
        assertThat(result.feedback()).contains("Minst èn stor bokstav");
        assertThat(result.feedback()).contains("Minst ett symbol");
    }

    @Test
    void validate_whenPasswordIsNull_shouldTreatAsEmptyString() {
        PasswordStrengthOption rule = rule(
                1,
                true,
                true,
                true,
                true,
                "Sterkt passord",
                "Svakt passord"
        );

        PasswordStrengthAnswer answer = passwordAnswer(null);

        ValidationResult result = validator.validate(List.of(rule), answer);

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).contains("Minst 1 tegn");
        assertThat(result.feedback()).contains("Minst ett tall");
        assertThat(result.feedback()).contains("Minst èn stor bokstav");
        assertThat(result.feedback()).contains("Minst èn liten bokstav");
        assertThat(result.feedback()).contains("Minst ett symbol");
    }

    @Test
    void validate_whenSubmissionIsNotPasswordStrengthAnswer_shouldThrowIllegalArgumentException() {
        PasswordStrengthOption rule = rule(
                8,
                true,
                true,
                true,
                true,
                "Sterkt passord",
                "Svakt passord"
        );

        SubmitAnswerRequest wrongSubmission = mock(SubmitAnswerRequest.class);

        assertThatThrownBy(() -> validator.validate(List.of(rule), wrongSubmission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PASSWORD_STRENGTH requires PasswordStrengthAnswer");
    }

    @Test
    void validate_whenOptionsAreEmpty_shouldThrowIndexOutOfBoundsException() {
        PasswordStrengthAnswer answer = passwordAnswer("Strong1!");

        assertThatThrownBy(() -> validator.validate(List.of(), answer))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    private PasswordStrengthOption rule(
            int minLength,
            boolean requireDigit,
            boolean requireUpper,
            boolean requireLower,
            boolean requireSymbol,
            String successFeedback,
            String failureFeedback
    ) {
        PasswordStrengthOption option = mock(PasswordStrengthOption.class);

        when(option.getMinLength()).thenReturn(minLength);
        when(option.isRequireDigit()).thenReturn(requireDigit);
        when(option.isRequireUpper()).thenReturn(requireUpper);
        when(option.isRequireLower()).thenReturn(requireLower);
        when(option.isRequireSymbol()).thenReturn(requireSymbol);
        when(option.getSuccessFeedback()).thenReturn(successFeedback);
        when(option.getFailureFeedback()).thenReturn(failureFeedback);

        return option;
    }

    private PasswordStrengthAnswer passwordAnswer(String password) {
        PasswordStrengthAnswer answer = mock(PasswordStrengthAnswer.class);
        when(answer.getPassword()).thenReturn(password);
        return answer;
    }
}