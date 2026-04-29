package org.ntnu.backend.avatar.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for equipped items. It packages and transports the wished and 
 * spesific data between backend to frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquippedItemDto {
    private UUID id;
    private String name;
    private String category;
    private String imageUrl;
    private boolean locked;
}
