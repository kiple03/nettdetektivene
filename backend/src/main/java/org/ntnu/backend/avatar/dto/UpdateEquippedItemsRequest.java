package org.ntnu.backend.avatar.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for an update on specific items. It structures
 * data from frontend to backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquippedItemsRequest {
    private List<UUID> itemIds;
}
