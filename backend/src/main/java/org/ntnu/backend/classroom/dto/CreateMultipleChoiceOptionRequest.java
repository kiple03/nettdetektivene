package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a single multiple-choice option within a question.
 *
 * @param text             the display text of the option
 * @param imageUrl         optional URL of an image shown to the student while answering; {@code null} means no image
 * @param responseImageUrl optional URL of an image shown after the student submits (e.g. annotated version);
 *                         {@code null} means no after-image
 * @param isCorrect        whether this option is the correct answer
 * @param explanation      feedback shown to the student after they answer
 */
public record CreateMultipleChoiceOptionRequest(
    @NotBlank String text,
    String imageUrl,
    String responseImageUrl,
    boolean isCorrect,
    @NotBlank String explanation
) {}
