package org.ntnu.backend.game.validation;

import java.util.List;

/**
 * Result of validating a student's submitted answer.
 *
 * @param correct              whether the submission matches the correct answer
 * @param feedback             explanation shown to the student, or null if none needed
 * @param correctOptionIds     IDs of the options that are correct (empty for non-option questions)
 * @param wrongSelectedOptionIds IDs of options the student selected that were wrong
 */
public record ValidationResult(
        boolean correct,
        String feedback,
        List<String> correctOptionIds,
        List<String> wrongSelectedOptionIds
) {
  /**
   * Creates a result without option-level detail.
   *
   * @param correct  whether the answer is correct
   * @param feedback explanation for the student
   * @return a {@code ValidationResult} with empty option ID lists
   */
  public static ValidationResult of(boolean correct, String feedback) {
    return new ValidationResult(correct, feedback, List.of(), List.of());
  }
}