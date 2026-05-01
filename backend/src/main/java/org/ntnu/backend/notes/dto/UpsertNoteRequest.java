package org.ntnu.backend.notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a note.
 *
 * @param content the note text; must be non-blank and at most 5000 characters
 */
public record UpsertNoteRequest(
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 5000, message = "Content cannot exceed 5000 characters")
        String content
) {}