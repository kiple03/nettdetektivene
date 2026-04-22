package org.ntnu.backend.classroom.dto;

import java.util.List;

import org.ntnu.backend.avatar.dto.EquippedItemDto;

import lombok.Builder;
import lombok.Getter;

/** Data Transfer Object for student progress */
@Getter
@Builder
public class StudentProgressResponse {
    private String userId;
    private String username;
    private int levelsCompleted;
    private long medalsEarned;
    private List<EquippedItemDto> equippedItems;

    public StudentProgressResponse(String userId, String username, int levelsCompleted, long medalsEarned,
                                   List<EquippedItemDto> equippedItems) {
        this.userId = userId;
        this.username = username;
        this.levelsCompleted = levelsCompleted;
        this.medalsEarned = medalsEarned;
        this.equippedItems = equippedItems;
    }
}
