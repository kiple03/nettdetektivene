package org.ntnu.backend.classroom.dto;

import java.util.List;

import org.ntnu.backend.avatar.dto.EquippedItemDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Data Transfer Object for student progress */
@Getter
@Builder
@AllArgsConstructor
public class StudentProgressResponse {
    private final String userId;
    private final String username;
    private final String email;
    private final int currentStopDisplayOrder;
    private final int totalStops;
    private final int score;
    private final int medalsEarned;
    private final List<EquippedItemDto> equippedItems;
    private final String hairColor;
}
