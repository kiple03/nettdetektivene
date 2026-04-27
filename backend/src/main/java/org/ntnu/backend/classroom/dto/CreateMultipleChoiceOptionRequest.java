package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMultipleChoiceOptionRequest(
    @NotBlank String text,
    String imageUrl,
    boolean isCorrect,
    @NotBlank String explanation
) {}
