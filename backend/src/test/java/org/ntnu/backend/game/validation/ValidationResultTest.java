package org.ntnu.backend.game.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ValidationResultTest {

    @Test
    void of_shouldCreateResultWithEmptyCorrectAndWrongOptionLists() {
        ValidationResult result = ValidationResult.of(true, "Correct feedback");

        assertThat(result.correct()).isTrue();
        assertThat(result.feedback()).isEqualTo("Correct feedback");
        assertThat(result.correctOptionIds()).isEqualTo(List.of());
        assertThat(result.wrongSelectedOptionIds()).isEqualTo(List.of());
    }

    @Test
    void constructor_shouldStoreAllFields() {
        ValidationResult result = new ValidationResult(
                false,
                "Wrong feedback",
                List.of(1, 2),
                List.of(3)
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.feedback()).isEqualTo("Wrong feedback");
        assertThat(result.correctOptionIds()).containsExactly(1, 2);
        assertThat(result.wrongSelectedOptionIds()).containsExactly(3);
    }
}