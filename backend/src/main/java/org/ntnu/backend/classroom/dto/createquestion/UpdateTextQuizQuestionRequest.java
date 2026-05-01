package org.ntnu.backend.classroom.dto.createquestion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request payload for updating an existing text-quiz question.
 *
 * @param questionText the updated question prompt; must not be blank
 * @param options      the updated list of answer options; must contain at least one valid entry
 */
public record UpdateTextQuizQuestionRequest(
    @NotBlank String questionText,
    @NotEmpty List<@Valid CreateTextQuizOptionRequest> options
) implements UpdateTeacherQuestionRequest {}
