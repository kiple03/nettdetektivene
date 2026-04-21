package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for level completion.
 */
@Getter
@Builder
@AllArgsConstructor
public class LevelCompletionEntry {
    private int levelId;
    private Instant completedAt;
}
