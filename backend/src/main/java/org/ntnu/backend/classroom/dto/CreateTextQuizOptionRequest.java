package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTextQuizOptionRequest(
    @NotBlank String text,
    boolean isCorrect,
    @NotBlank String explanation
) {}
