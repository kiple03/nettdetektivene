package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCensorWordRequest(
    @NotBlank
    @Size(max = 50)
    String word
) {}
