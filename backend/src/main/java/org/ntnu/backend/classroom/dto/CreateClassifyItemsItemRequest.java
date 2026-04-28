package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateClassifyItemsItemRequest(
    @NotBlank String text,
    @NotBlank String correctLabel,
    @NotBlank String explanation
) {}
