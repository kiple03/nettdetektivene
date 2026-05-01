package org.ntnu.backend.game.dto.progress;

import java.util.UUID;

/**
 * Summary of a user's overall game progress.
 *
 * @param userId                 the user this progress belongs to
 * @param totalLevelsCompleted   total number of levels the user has finished
 * @param currentStopDisplayOrder display order of the stop the user is currently at
 * @param currentStopName        name of the stop the user is currently at
 * @param currentLevelOrder      order of the level within the current stop
 * @param gameCompleted          whether the user has completed the entire game
 */
public record ProgressSummaryResponse(
    UUID userId,
    int totalLevelsCompleted,
    int currentStopDisplayOrder,
    String currentStopName,
    int currentLevelOrder,
    boolean gameCompleted
) {}