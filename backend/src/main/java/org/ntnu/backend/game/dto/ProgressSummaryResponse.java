package org.ntnu.backend.game.dto;

import java.util.UUID;

public record ProgressSummaryResponse(
    UUID userId,
    int totalLevelsCompleted,
    int currentStopDisplayOrder,
    String currentStopName,
    int currentLevelOrder,
    boolean gameCompleted
) {}