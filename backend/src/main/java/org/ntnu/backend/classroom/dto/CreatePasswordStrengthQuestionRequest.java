package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a password-strength question in a classroom stop.
 *
 * @param questionText    the question prompt shown to the student
 * @param minLength       minimum required password length (1–64)
 * @param requireDigit    whether the password must contain at least one digit
 * @param requireUpper    whether the password must contain at least one uppercase letter
 * @param requireLower    whether the password must contain at least one lowercase letter
 * @param requireSymbol   whether the password must contain at least one special character
 * @param successFeedback feedback shown when the student's password meets all requirements
 * @param failureFeedback feedback shown when the student's password fails validation
 */
public record CreatePasswordStrengthQuestionRequest(
    @NotBlank String questionText,
    @Min(1) @Max(64) int minLength,
    boolean requireDigit,
    boolean requireUpper,
    boolean requireLower,
    boolean requireSymbol,
    @NotBlank String successFeedback,
    @NotBlank String failureFeedback
) implements CreateTeacherQuestionRequest {}
