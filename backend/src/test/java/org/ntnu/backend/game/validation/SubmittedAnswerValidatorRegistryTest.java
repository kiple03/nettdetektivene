package org.ntnu.backend.game.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.model.enums.LevelType;

class SubmittedAnswerValidatorRegistryTest {

    @Test
    void forType_whenValidatorIsRegistered_shouldReturnMatchingValidator() {
        SubmittedAnswerValidator multipleChoiceValidator =
                validatorFor(LevelType.MULTIPLE_CHOICE);

        SubmittedAnswerValidator postOfficeValidator =
                validatorFor(LevelType.POST_OFFICE);

        SubmittedAnswerValidatorRegistry registry =
                new SubmittedAnswerValidatorRegistry(List.of(
                        multipleChoiceValidator,
                        postOfficeValidator
                ));

        SubmittedAnswerValidator result =
                registry.forType(LevelType.MULTIPLE_CHOICE);

        assertThat(result).isSameAs(multipleChoiceValidator);
    }

    @Test
    void forType_whenDifferentValidatorIsRequested_shouldReturnCorrectValidator() {
        SubmittedAnswerValidator multipleChoiceValidator =
                validatorFor(LevelType.MULTIPLE_CHOICE);

        SubmittedAnswerValidator postOfficeValidator =
                validatorFor(LevelType.POST_OFFICE);

        SubmittedAnswerValidatorRegistry registry =
                new SubmittedAnswerValidatorRegistry(List.of(
                        multipleChoiceValidator,
                        postOfficeValidator
                ));

        SubmittedAnswerValidator result =
                registry.forType(LevelType.POST_OFFICE);

        assertThat(result).isSameAs(postOfficeValidator);
    }

    @Test
    void forType_whenValidatorIsNotRegistered_shouldThrowIllegalStateException() {
        SubmittedAnswerValidator multipleChoiceValidator =
                validatorFor(LevelType.MULTIPLE_CHOICE);

        SubmittedAnswerValidatorRegistry registry =
                new SubmittedAnswerValidatorRegistry(List.of(multipleChoiceValidator));

        assertThatThrownBy(() -> registry.forType(LevelType.POST_OFFICE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No validator registered for level type: POST_OFFICE");
    }

    @Test
    void forType_whenRegistryIsEmpty_shouldThrowIllegalStateException() {
        SubmittedAnswerValidatorRegistry registry =
                new SubmittedAnswerValidatorRegistry(List.of());

        assertThatThrownBy(() -> registry.forType(LevelType.MULTIPLE_CHOICE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No validator registered for level type: MULTIPLE_CHOICE");
    }

    @Test
    void constructor_whenDuplicateValidatorsForSameType_shouldThrowIllegalStateException() {
        SubmittedAnswerValidator firstValidator =
                validatorFor(LevelType.MULTIPLE_CHOICE);

        SubmittedAnswerValidator secondValidator =
                validatorFor(LevelType.MULTIPLE_CHOICE);

        assertThatThrownBy(() -> new SubmittedAnswerValidatorRegistry(List.of(
                firstValidator,
                secondValidator
        )))
                .isInstanceOf(IllegalStateException.class);
    }

    private SubmittedAnswerValidator validatorFor(LevelType levelType) {
        SubmittedAnswerValidator validator = mock(SubmittedAnswerValidator.class);
        when(validator.supportedType()).thenReturn(levelType);
        return validator;
    }
}