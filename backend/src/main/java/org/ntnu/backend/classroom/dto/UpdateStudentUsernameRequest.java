package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStudentUsernameRequest(
    @NotBlank
    @Size(min = 3, max = 20)
    String username
) {}
