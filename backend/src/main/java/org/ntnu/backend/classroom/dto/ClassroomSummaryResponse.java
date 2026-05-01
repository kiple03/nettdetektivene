package org.ntnu.backend.classroom.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Summary view of a classroom, returned in list endpoints where full detail is not needed.
 * Includes aggregate progress statistics across all enrolled students.
 */
@Getter
@Builder
@AllArgsConstructor
public class ClassroomSummaryResponse {
    private String id;
    private String title;
    private String description;

    /** Short alphanumeric code students use to join the classroom. */
    private String joinCode;
    private LocalDateTime createdAt;
    private long studentCount;

    private int totalStops;

    /** Average map-stop position reached across all students (fractional). */
    private double avgStopPosition;

    /** Average score across all students in this classroom. */
    private int avgScore;

    /** Percentage of the game content completed on average by students (0–100). */
    private int progressPercent;
    private List<String> teacherUsernames;
}