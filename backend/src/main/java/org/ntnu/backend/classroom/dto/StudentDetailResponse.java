package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for student info
 */
@Getter
@Builder
@AllArgsConstructor
public class StudentDetailResponse {
    private String username;
    private String email;
    private Instant enrolledAt;
    private String currentStopName;
    private int currentLevelOrder;
    private int totalLevelsCompleted;
    private boolean gameCompleted;
}