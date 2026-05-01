package org.ntnu.backend.avatar.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a user's avatar profile, returned on profile fetch requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    /** The student's display name. */
    private String username;

    /** Avatar items currently equipped by the user. */
    private List<EquippedItemDto> equippedItems;

    /** Hex or named colour applied to the avatar's hair. */
    private String hairColor;
}
