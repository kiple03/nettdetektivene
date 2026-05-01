package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a new classroom.
 */
@Data
public class CreateClassroomRequest {

    /** Display name of the classroom; 2–80 characters, required. */
    @NotBlank(message = "Klassenavn er påkrevd")
    @Size(min = 2, max = 80)
    private String title;

    /** Optional description of the classroom; max 300 characters. */
    @Size(max = 300)
    private String description;
}