package org.ntnu.backend.classroom.dto.createquestion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for creating a text-based quiz question where students pick from text answer options.
 *
 * @param questionText the prompt shown to students; must not be blank
 * @param options      list of answer options; at least one required, each validated individually
 */
public record CreateTextQuizQuestionRequest(
    @NotBlank String questionText,
    @NotEmpty List<@Valid CreateTextQuizOptionRequest> options
) implements CreateTeacherQuestionRequest {}
