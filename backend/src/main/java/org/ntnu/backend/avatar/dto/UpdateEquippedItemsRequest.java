package org.ntnu.backend.avatar.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for replacing a user's full set of equipped avatar items.
 * All previously equipped items are replaced with the provided list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquippedItemsRequest {
    /** UUIDs of the avatar items to equip; replaces the current equipped set. */
    private List<UUID> itemIds;
}
