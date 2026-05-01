package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for joining a classroom with code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinClassroomRequest {

    /** Exactly 6-character code displayed to students by the teacher. */
    @NotBlank(message = "Bli-med-kode kan ikke være tom")
    @Size(min = 6, max = 6, message = "Bli-med-kode må være nøyaktig 6 tegn")
    private String joinCode;
}