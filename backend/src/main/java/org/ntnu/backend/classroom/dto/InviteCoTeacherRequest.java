package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for inviting a co-teacher to a classroom.
 *
 * @param email the email address of the teacher to invite; must be a valid, non-blank email
 */
public record InviteCoTeacherRequest(
    @NotBlank
    @Email
    String email
) {}
