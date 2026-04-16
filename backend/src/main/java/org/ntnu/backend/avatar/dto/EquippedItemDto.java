package org.ntnu.backend.avatar.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquippedItemDto {
    private UUID id;
    private String name;
    private String category;
    private String imageUrl;
}
