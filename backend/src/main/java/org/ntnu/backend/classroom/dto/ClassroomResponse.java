package org.ntnu.backend.classroom.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO representing a classroom's full details.
 */
@Getter
@Builder
public class ClassroomResponse {
    private String id;
    private String title;
    private String description;
    /** Short alphanumeric code students enter to join this classroom. */
    private String joinCode;
    private LocalDateTime createdAt;
}