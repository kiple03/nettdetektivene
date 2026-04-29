package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdatePictureQuizQuestionRequest(
    @NotBlank String questionText,
    @Size(min = 2, max = 6) List<@Valid CreatePictureQuizOptionRequest> options
) implements UpdateTeacherQuestionRequest {}
