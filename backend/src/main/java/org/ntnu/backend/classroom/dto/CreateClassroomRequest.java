package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for creation of a classroom. 
 */
@Data
public class CreateClassroomRequest {

    @NotBlank(message = "Klassenavn er påkrevd")
    @Size(min = 2, max = 80)
    private String title;

    @Size(max = 300)
    private String description;
}