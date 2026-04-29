package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

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
