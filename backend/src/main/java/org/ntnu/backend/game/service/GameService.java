package org.ntnu.backend.game.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.progress.SubmitLevelRequest;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.validation.LevelSubmissionValidator;
import org.ntnu.backend.notes.service.NoteService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private final QuestionRepository questionRepository;
  private final ProgressService progressService;
  private final GameMapper gameMapper;
  private final LevelSubmissionValidator levelSubmissionValidator;
  private final NoteService noteService;


  /**
   * Returns all stops for the game map, ordered by display position.
   *
   * <p>Each stop includes its lock status for the given student.
   *
   * @param userId the student requesting the map (unused until lock logic is added)
   * @return all stops with per-student lock state
   */
  @Transactional(readOnly = true)
  public List<StopResponse> getStopsForUser(UUID userId, RoleEnum role) {
    return stopRepository.findAllByOrderByDisplayOrderAsc()
        .stream()
        .map(stop -> gameMapper.toStopOverviewResponse(
            stop,
            !progressService.isStopAccessible(userId, role, stop)
        )).toList();
  }

  /**
   * Returns a single level's metadata for the intro/guidance screen.
   *
   * @param levelId the level to fetch
   * @param userId  the student requesting (unused until lock logic is added)
   * @return the level details including type and description
   * @throws LevelNotFoundException if no level exists with this ID
   */
  @Transactional(readOnly = true)
  public LevelResponse getLevel(int levelId, UUID userId, RoleEnum role) {
    return levelRepository.findById(levelId)
        .map(level -> gameMapper.toLevelResponse(
            level,
            !progressService.isLevelAccessible(userId, role, level)
        )).orElseThrow(() -> new LevelNotFoundException(levelId));
  }

  /**
   * Returns all levels for a stop, ordered for display, with per-user lock state applied.
   *
   * @param stopId the stop whose levels to fetch
   * @param userId the requesting user
   * @param role   the user's role, used to determine accessible levels
   * @return ordered list of levels with lock status
   */
  @Transactional(readOnly = true)
  public List<LevelResponse> getLevelsForStop(int stopId, UUID userId, RoleEnum role) {
    return levelRepository.findByStopIdOrderByOrderAsc(stopId)
        .stream()
        .map(level -> gameMapper.toLevelResponse(
            level,
            !progressService.isLevelAccessible(userId, role, level)
        )).toList();
  }

  /**
   * Bypasses answer validation and marks a level as completed — for admin/cheat use only.
   * Also saves the completion tip note if this is the first completion.
   *
   * @param levelId   the level to complete
   * @param principal the authenticated admin/teacher performing the action
   * @throws LevelNotFoundException if no level exists with this ID
   */
  @Transactional
  public void adminCompleteLevel(int levelId, JwtAuthenticatedPrincipal principal) {
    Level level = levelRepository.findById(levelId)
        .orElseThrow(() -> new LevelNotFoundException(levelId));
    boolean newlyCompleted = progressService.recordCompletionOfLevel(
        principal.getUserId(), principal.getRole(), level);
    if (newlyCompleted) {
      noteService.saveCompletionTipIfNeeded(principal.getUserId(), level);
    }
  }

  /**
   * Validates the student's answers for a level and records completion if they pass.
   * Questions are scoped to the student's classroom when one is active; otherwise
   * only default (non-classroom) questions are used.
   *
   * @param levelId   the level being submitted
   * @param request   the student's answers
   * @param principal the authenticated student
   * @throws LevelNotFoundException         if no level exists with this ID
   * @throws org.ntnu.backend.exception.InvalidLevelSubmissionException if answers fail validation
   */
  @Transactional
  public void submitLevel(int levelId, SubmitLevelRequest request, JwtAuthenticatedPrincipal principal) {
    Level level = levelRepository.findById(levelId)
        .orElseThrow(() -> new LevelNotFoundException(levelId));

    UUID classroomId = principal.getClassroomId();
    List<Question> questions = (principal.isStudent() && classroomId != null)
        ? questionRepository.findByLevelIdForClassroom(levelId, classroomId)
        : questionRepository.findByLevelIdAndClassroomIdIsNull(levelId);

    levelSubmissionValidator.validate(questions, request.answers());

    boolean newlyCompleted = progressService.recordCompletionOfLevel(principal.getUserId(), principal.getRole(), level);
    if (newlyCompleted) {
      noteService.saveCompletionTipIfNeeded(principal.getUserId(), level);
    }
  }
}
