package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for updating a classroom.
 */
@Data
public class UpdateClassroomRequest {

    /** Display name of the classroom; must be 2–80 characters. */
    @Size(min = 2, max = 80)
    private String title;

    /** Optional description shown to students; max 300 characters. */
    @Size(max = 300)
    private String description;
}