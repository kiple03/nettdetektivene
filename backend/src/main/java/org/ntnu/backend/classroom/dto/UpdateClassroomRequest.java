package org.ntnu.backend.classroom.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateClassroomRequest {

    @Size(min = 2, max = 80)
    private String title;

    @Size(max = 300)
    private String description;
}