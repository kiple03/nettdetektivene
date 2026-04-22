package org.ntnu.backend.notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertNoteRequest(
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 5000, message = "Content cannot exceed 5000 characters")
        String content
) {}