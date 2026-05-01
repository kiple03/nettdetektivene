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
    /** UUID of the classroom. */
    private String id;

    private String title;

    private String description;

    /** Short alphanumeric code students use to join the classroom. */
    private String joinCode;

    private LocalDateTime createdAt;

    /** JWT access token re-issued with the classroomId claim injected. */
    private String accessToken;
}
