package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for creating a picture quiz question.
 * Each option pairs an image with a correct/incorrect flag.
 *
 * @param questionText the prompt shown to the student
 * @param options      between 2 and 6 image options for this question
 */
public record CreatePictureQuizQuestionRequest(
    @NotBlank String questionText,
    @Size(min = 2, max = 6) List<@Valid CreatePictureQuizOptionRequest> options
) implements CreateTeacherQuestionRequest {}
