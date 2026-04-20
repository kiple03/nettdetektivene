package org.ntnu.backend.classroom.dto;

import java.util.List;
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
    private List<LevelCompletionEntry> completions;
}
