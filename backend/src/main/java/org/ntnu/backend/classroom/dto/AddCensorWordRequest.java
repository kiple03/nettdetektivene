package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for adding a censor word to a classroom.
 * The word must be non-blank and at most 50 characters.
 */
public record AddCensorWordRequest(
    @NotBlank
    @Size(max = 50)
    String word
) {}
