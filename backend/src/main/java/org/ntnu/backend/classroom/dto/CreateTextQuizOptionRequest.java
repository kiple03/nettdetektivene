package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a single answer option in a text quiz question.
 *
 * @param text the display text of the option
 * @param isCorrect whether this option is the correct answer
 * @param explanation shown to the student after answering, explaining why this option is correct or incorrect
 */
public record CreateTextQuizOptionRequest(
    @NotBlank String text,
    boolean isCorrect,
    @NotBlank String explanation
) {}
