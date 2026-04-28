package org.ntnu.backend.medalion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response data transfer object for a medalion.
 * Returned when fetching or awarding medalions for a user.
 * The {@code imageUrl} is constructed by the service layer and points to a static frontend asset.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedalionResponse {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime earnedAt;
    private String imageUrl;
}
