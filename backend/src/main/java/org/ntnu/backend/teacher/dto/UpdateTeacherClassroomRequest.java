package org.ntnu.backend.teacher.dto;

import jakarta.validation.constraints.Size;

public class UpdateTeacherClassroomRequest {

    @Size(max = 200)
    private String title;

    private String description;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
