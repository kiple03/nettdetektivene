package org.ntnu.backend.classroom.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * Response returned when a student successfully joins a classroom.
 * Includes a fresh access token with the classroomId claim encoded.
 */
@Getter
@Builder
public class JoinClassroomResponse {
    private String id;
    private String title;
    private String description;
    private String joinCode;
    private LocalDateTime createdAt;
    private String accessToken;
}
