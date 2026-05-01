package org.ntnu.backend.classroom.dto.createquestion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating a password-strength question.
 *
 * @param questionText    the question prompt shown to the student
 * @param minLength       minimum accepted password length (1–64)
 * @param requireDigit    whether at least one digit is required
 * @param requireUpper    whether at least one uppercase letter is required
 * @param requireLower    whether at least one lowercase letter is required
 * @param requireSymbol   whether at least one special character is required
 * @param successFeedback feedback shown when the student's answer is correct
 * @param failureFeedback feedback shown when the student's answer is incorrect
 */
public record UpdatePasswordStrengthQuestionRequest(
    @NotBlank String questionText,
    @Min(1) @Max(64) int minLength,
    boolean requireDigit,
    boolean requireUpper,
    boolean requireLower,
    boolean requireSymbol,
    @NotBlank String successFeedback,
    @NotBlank String failureFeedback
) implements UpdateTeacherQuestionRequest {}
