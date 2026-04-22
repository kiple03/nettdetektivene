package org.ntnu.backend.game.mapper;

import java.util.List;
import org.ntnu.backend.game.dto.ProgressSummaryResponse;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.StopLevelCount;
import org.springframework.stereotype.Component;

/**
 * Maps {@link UserProgress} entities to {@link ProgressSummaryResponse} DTOs.
 *
 * <p>Computing a summary requires knowing the level counts of every stop
 * in the game, so callers must fetch {@link StopLevelCount} data once and
 * pass it in. This avoids per-student DB queries when mapping many
 * students at once.
 */
@Component
public class ProgressMapper {

  /**
   * Builds a summary for a single student's progress.
   *
   * @param progress the student's progress row (with currentStop fetched)
   * @param stopLevelCounts level counts for every stop in the game
   * @param totalLevelsInGame pre-computed sum of all level counts
   *
   * @return a summary describing the student's position and completion count
   */
  public ProgressSummaryResponse toSummary(
      UserProgress progress,
      List<StopLevelCount> stopLevelCounts,
      int totalLevelsInGame
  ) {
    int totalCompleted = computeTotalLevelsCompleted(progress, stopLevelCounts, totalLevelsInGame);
    Stop currentStop = progress.getCurrentStop();

    return new ProgressSummaryResponse(
        progress.getId(),
        totalCompleted,
        currentStop.getDisplayOrder(),
        currentStop.getName(),
        progress.getCurrentLevelOrder(),
        progress.isGameCompleted()
    );
  }

  private int computeTotalLevelsCompleted(
      UserProgress progress,
      List<StopLevelCount> stopLevelCounts,
      int totalLevelsInGame
  ) {

    if (progress.isGameCompleted()) {
      return totalLevelsInGame;
    }

    int currentStopOrder = progress.getCurrentStop().getDisplayOrder();
    int levelsFromPastStops = stopLevelCounts.stream()
        .filter(s -> s.getDisplayOrder() < currentStopOrder)
        .mapToInt(s -> (int) s.getLevelCount())
        .sum();

    return levelsFromPastStops + (progress.getCurrentLevelOrder() - 1);
  }
}