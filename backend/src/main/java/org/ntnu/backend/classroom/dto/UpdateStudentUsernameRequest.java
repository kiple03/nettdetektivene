package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a student's display username within a classroom.
 */
public record UpdateStudentUsernameRequest(
    /** New username; must be 3–30 characters and non-blank. */
    @NotBlank
    @Size(min = 3, max = 30)
    String username
) {}
