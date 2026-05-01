package org.ntnu.backend.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO representing a co-teacher associated with a classroom.
 */
@Getter
@Builder
@AllArgsConstructor
public class CoTeacherResponse {
    private String userId;
    private String username;
    private String email;
    /** Whether this teacher is the primary owner of the classroom. */
    private boolean owner;
}
