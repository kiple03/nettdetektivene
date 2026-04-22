package org.ntnu.backend.game.dto.progress;

import java.util.UUID;

public record ProgressSummaryResponse(
    UUID userId,
    int totalLevelsCompleted,
    int currentStopDisplayOrder,
    String currentStopName,
    int currentLevelOrder,
    boolean gameCompleted
) {}