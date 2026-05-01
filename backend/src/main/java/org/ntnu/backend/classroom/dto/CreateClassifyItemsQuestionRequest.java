package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for creating a classify-items question, where students drag items into labelled categories.
 *
 * @param questionText the question prompt shown to the student
 * @param labels       the category labels items can be sorted into; at least 2 required
 * @param items        the individual items students must classify
 */
public record CreateClassifyItemsQuestionRequest(
    @NotBlank String questionText,
    @Size(min = 2) List<@NotBlank String> labels,
    @NotEmpty List<@Valid CreateClassifyItemsItemRequest> items
) implements CreateTeacherQuestionRequest {}
