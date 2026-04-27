package org.ntnu.backend.classroom.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import org.ntnu.backend.avatar.dto.EquippedItemDto;

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
    private int score;
    private int medalsEarned;
    private List<EquippedItemDto> equippedItems;
}
