package org.ntnu.backend.game.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.springframework.stereotype.Service;

/**
 * Handles the game map and level navigation. Provides stop overviews
 * for the map view and level details for the intro screen.
 *
 * <p>Lock/unlock logic is not yet implemented, all stops and levels
 * are currently returned as unlocked. When progression tracking is
 * added, this service will check the student's completion state
 * before determining lock status.</p>
 *
 * @see QuestionService
 * @see GameMapper
 */
@Service
@AllArgsConstructor
public class GameService {

  private final StopRepository stopRepository;
  private final LevelRepository levelRepository;
  private final GameMapper gameMapper;


  // needs to set lockead/unlocked later
  // hgardcoded all unlocked

  /**
   * Returns all stops for the game map, ordered by display position.
   *
   * <p>Each stop includes its lock status for the given student.
   * Currently hardcoded to unlocked, will use completion data
   * from enrollment/progress tables once implemented.
   *
   * @param userId the student requesting the map (unused until lock logic is added)
   * @return all stops with per-student lock state
   */
  public List<StopResponse> getStopsForUser(UUID userId) {
    return stopRepository.findAll()
        .stream()
        .map(stop -> gameMapper.toStopOverviewResponse(stop, false))
        .toList();
  }

  /**
   * Returns a single level's metadata for the intro/guidance screen.
   *
   * @param levelId the level to fetch
   * @param userId  the student requesting (unused until lock logic is added)
   * @return the level details including type and description
   * @throws LevelNotFoundException if no level exists with this ID
   */
  public LevelResponse getLevel(int levelId, UUID userId) {
    return levelRepository.findById(levelId)
        .map(level -> gameMapper.toLevelResponse(level, false))
        .orElseThrow(() -> new LevelNotFoundException(levelId));
  }
}
