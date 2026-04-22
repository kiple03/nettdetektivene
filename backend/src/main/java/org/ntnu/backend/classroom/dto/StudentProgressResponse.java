package org.ntnu.backend.classroom.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.ntnu.backend.avatar.dto.EquippedItemDto;

/** Data Transfer Object for student progress */
@Getter
@Builder
@AllArgsConstructor
public class StudentProgressResponse {
    private String userId;
    private String username;
    private int levelsCompleted;
    private long medalsEarned;
    private List<EquippedItemDto> equippedItems;
}
