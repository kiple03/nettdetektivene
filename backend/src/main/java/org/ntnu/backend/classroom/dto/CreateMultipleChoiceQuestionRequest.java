package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for creating a multiple-choice question in a classroom level.
 *
 * @param questionText the question prompt shown to students
 * @param options      the answer options, at least one required; each option is validated individually
 */
public record CreateMultipleChoiceQuestionRequest(
    @NotBlank String questionText,
    @NotEmpty List<@Valid CreateMultipleChoiceOptionRequest> options
) implements CreateTeacherQuestionRequest {}
