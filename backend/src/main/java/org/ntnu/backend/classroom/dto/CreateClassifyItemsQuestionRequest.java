package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateClassifyItemsQuestionRequest(
    @NotBlank String questionText,
    @Size(min = 2) List<@NotBlank String> labels,
    @NotEmpty List<@Valid CreateClassifyItemsItemRequest> items
) implements CreateTeacherQuestionRequest {}
