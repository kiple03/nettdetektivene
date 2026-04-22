package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteCoTeacherRequest(
    @NotBlank
    @Email
    String email
) {}
