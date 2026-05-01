package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for updating a classify-items question.
 *
 * @param questionText the updated prompt shown to students
 * @param labels       category labels items are sorted into; at least 2 required
 * @param items        the items students must classify; must not be empty
 */
public record UpdateClassifyItemsQuestionRequest(
    @NotBlank String questionText,
    @Size(min = 2) List<@NotBlank String> labels,
    @NotEmpty List<@Valid CreateClassifyItemsItemRequest> items
) implements UpdateTeacherQuestionRequest {}
