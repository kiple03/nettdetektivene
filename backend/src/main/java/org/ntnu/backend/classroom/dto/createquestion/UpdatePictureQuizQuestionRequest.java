package org.ntnu.backend.classroom.dto.createquestion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for updating an existing picture quiz question.
 *
 * @param questionText the updated question prompt
 * @param options      2–6 picture options; each option is validated individually
 */
public record UpdatePictureQuizQuestionRequest(
    @NotBlank String questionText,
    @Size(min = 2, max = 6) List<@Valid CreatePictureQuizOptionRequest> options
) implements UpdateTeacherQuestionRequest {}
