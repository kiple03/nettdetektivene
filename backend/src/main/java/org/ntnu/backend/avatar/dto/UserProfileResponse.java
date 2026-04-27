package org.ntnu.backend.avatar.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for user profile get-request. It structuers
 * only the neccessary data for the frontend to recieve. 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String username;
    private List<EquippedItemDto> equippedItems;
    private String hairColor;
}
