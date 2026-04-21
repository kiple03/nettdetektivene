package org.ntnu.backend.classroom.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for classroom respone.
 */
@Getter
@Builder
public class ClassroomResponse {
    private String id;
    private String title;
    private String description;
    private String joinCode;
    private LocalDateTime createdAt;
}