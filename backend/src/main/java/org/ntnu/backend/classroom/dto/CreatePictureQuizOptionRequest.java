package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a single picture-quiz answer option.
 *
 * @param text        display label shown alongside the image
 * @param imageUrl    URL of the image associated with this option
 * @param isCorrect   whether this option is the correct answer
 * @param explanation feedback shown to the student after answering
 */
public record CreatePictureQuizOptionRequest(
    @NotBlank String text,
    @NotBlank String imageUrl,
    boolean isCorrect,
    @NotBlank String explanation
) {}
