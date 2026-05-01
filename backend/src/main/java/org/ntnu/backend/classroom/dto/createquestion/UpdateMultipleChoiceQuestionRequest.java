package org.ntnu.backend.classroom.dto.createquestion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for updating an existing multiple-choice question.
 *
 * @param questionText the revised question prompt; must not be blank
 * @param options      replacement list of answer options, each individually validated; must not be empty
 */
public record UpdateMultipleChoiceQuestionRequest(
    @NotBlank String questionText,
    @NotEmpty List<@Valid CreateMultipleChoiceOptionRequest> options
) implements UpdateTeacherQuestionRequest {}
