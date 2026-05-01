package org.ntnu.backend.classroom.dto.createquestion;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a single item in a classify-items question.
 *
 * @param text the display text of the item to be classified
 * @param correctLabel the label/category this item should be classified under
 * @param explanation feedback shown to the student after answering
 */
public record CreateClassifyItemsItemRequest(
    @NotBlank String text,
    @NotBlank String correctLabel,
    @NotBlank String explanation
) {}
