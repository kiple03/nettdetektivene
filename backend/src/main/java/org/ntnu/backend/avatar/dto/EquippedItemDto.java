package org.ntnu.backend.avatar.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single avatar item that a user has equipped or owns, returned as part of
 * the user's avatar/inventory response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquippedItemDto {
    /** Unique identifier of the avatar item. */
    private UUID id;
    private String name;
    /** Item category (e.g. "hair", "shirt", "accessory"). */
    private String category;
    private String imageUrl;
    /** Whether the item is locked and unavailable for the user to equip. */
    private boolean locked;
}
