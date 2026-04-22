package org.ntnu.backend.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Data Transfer Object for student progress */
@Getter
@Builder
@AllArgsConstructor
public class StudentProgressResponse {
    private String userId;
    private String username;
    private int levelsCompleted;
    private long medalsEarned;
}
