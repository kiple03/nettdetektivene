package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a single multiple-choice option within a question.
 *
 * @param text        the display text of the option
 * @param imageUrl    optional URL of an image to show alongside the text; {@code null} means no image
 * @param isCorrect   whether this option is the correct answer
 * @param explanation feedback shown to the student after they answer
 */
public record CreateMultipleChoiceOptionRequest(
    @NotBlank String text,
    String imageUrl,
    boolean isCorrect,
    @NotBlank String explanation
) {}
