package org.ntnu.backend.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StudentProgressResponse {
    private String userId;
    private String username;
    private long levelsCompleted;
    private long medalsEarned;
}
