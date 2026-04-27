package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateTeacherQuestionRequest(
    @NotBlank String questionText,
    @NotEmpty List<@Valid CreateMultipleChoiceOptionRequest> options
) {}
