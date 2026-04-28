package org.ntnu.backend.game.validation;

import java.util.List;

/**
 * Result of validating a student's submitted answer.
 *
 * @param correct whether the submission matches the correct answer
 * @param feedback explanation text for the student (explanation of why the
 *                 answer is right or wrong, or null if no feedback needed)
 */
public record ValidationResult(
        boolean correct,
        String feedback,
        List<Integer> correctOptionIds,
        List<Integer> wrongSelectedOptionIds
) {
  public static ValidationResult of(boolean correct, String feedback) {
    return new ValidationResult(correct, feedback, List.of(), List.of());
  }
}