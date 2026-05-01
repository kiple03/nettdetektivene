package org.ntnu.backend.classroom.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.ntnu.backend.avatar.dto.EquippedItemDto;

/**
 * Response DTO summarising a student's progress within a classroom,
 * including game advancement, score, and avatar customisation details.
 */
@Getter
@Builder
@AllArgsConstructor
public class StudentProgressResponse {
    /** Unique identifier of the student. */
    private final String userId;
    private final String username;
    private final String email;
    /** Display order of the stop the student is currently on (1-based). */
    private final int currentStopDisplayOrder;
    /** Total number of stops in the classroom's game route. */
    private final int totalStops;
    private final int score;
    /** Number of medals the student has earned so far. */
    private final int medalsEarned;
    /** Avatar items the student currently has equipped. */
    private final List<EquippedItemDto> equippedItems;
    /** CSS-compatible hair colour string for avatar rendering. */
    private final String hairColor;
}
