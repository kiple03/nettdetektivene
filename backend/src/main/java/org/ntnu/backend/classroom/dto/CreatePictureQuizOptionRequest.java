package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePictureQuizOptionRequest(
    @NotBlank String text,
    @NotBlank String imageUrl,
    boolean isCorrect,
    @NotBlank String explanation
) {}
