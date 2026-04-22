package org.ntnu.backend.classroom.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for classroom summary 
 */
@Getter
@Builder
@AllArgsConstructor
public class ClassroomSummaryResponse {
    private String id;
    private String title;
    private String description;
    private String joinCode;
    private LocalDateTime createdAt;
    private long studentCount;

    private int assignmentCount;
    private int completedCount;
    private int progressPercent;
    private List<String> teacherUsernames;
}