package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response payload with a student's profile and progress within a classroom.
 */
@Getter
@Builder
@AllArgsConstructor
public class StudentDetailResponse {
    private String username;
    private String email;
    /** Timestamp of when the student joined the classroom. */
    private Instant enrolledAt;
    /** Name of the map stop the student is currently at. */
    private String currentStopName;
    /** Order index of the level the student is currently on within their current stop. */
    private int currentLevelOrder;
    /** Total number of levels the student has completed across all stops. */
    private int totalLevelsCompleted;
    /** Whether the student has completed the entire game. */
    private boolean gameCompleted;
}