package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for updating a classroom.
 */
@Data
public class UpdateClassroomRequest {

    @Size(min = 2, max = 80)
    private String title;

    @Size(max = 300)
    private String description;
}