package org.ntnu.backend.game.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.ntnu.backend.exception.UserProgressNotFoundException;
import org.ntnu.backend.game.dto.progress.ProgressSummaryResponse;
import org.ntnu.backend.game.mapper.ProgressMapper;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopLevelCount;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ntnu.backend.medalion.service.MedalionService;

/**
 * Manages student progression through the game's stops and levels.
 *
 * <p>Each student has one {@link UserProgress} row, created at
 * registration via {@link #initProgressForStudent}. Teachers do not have
 * progress rows; they should have full access, and this is checked by the {@link RoleEnum}.
 *
 * <p>Progress is linear. A student advances one level at a time
 * within the current stop, and on completing the last level of a stop
 * advances to the first level of the next. Completing the last level of
 * the last stop sets {@link UserProgress#isGameCompleted()} to {@code true}.
 *
 * <p>Replaying already-completed levels is permitted (see
 * {@link #isLevelAccessible}) but does not affect stored progress
 *
 * @see UserProgress
 * @see GameService
 * @see QuestionService
 */
@Service
@AllArgsConstructor
public class ProgressService {

  private final UserProgressRepository userProgressRepository;
  private final LevelRepository levelRepository;
  private final StopRepository stopRepository;
  private final ProgressMapper progressMapper;
  private final MedalionService medalionService;
  
  /**
   * Creates the initial {@link UserProgress} row for a newly registered student,
   * pointing at the first level of the first stop.
   *
   * <p>Must be called only for users with role {@link RoleEnum#STUDENT}.
   * Teachers do not receive progress rows.
   *
   * @param user the freshly persisted student user
   * @throws IllegalStateException if no stops have been configured in the database, or
   *          tried to create progress for a teacher.
   */
  public void initProgressForStudent(User user) {
    if (user.getRole() == RoleEnum.TEACHER) {
      throw new IllegalStateException("Server misconfigured. Should not try to create "
          + "progress for teacher.");
    }

    Stop firstStop =  stopRepository.findFirstStop()
        .orElseThrow(() -> new IllegalStateException("First stop not configured"));

    UserProgress progress = UserProgress.createInitial(user, firstStop);

    userProgressRepository.save(progress);
  }

  /**
   * Determines whether the given user can access the given stop.
   *
   * <p>Teachers always have access. For students, a stop is accessible if
   * its {@link Stop#getDisplayOrder()} is at or before the student's
   * current stop, meaning previously completed stops remain accessible
   * for replay.
   *
   * @param userId the user whose access is being checked
   * @param role the user's role
   * @param stop the stop being checked
   * @return {@code true} if the user can access the stop, {@code false} otherwise
   * @throws UserProgressNotFoundException if the user is a student with no progress row
   */
  public boolean isStopAccessible(UUID userId, RoleEnum role, Stop stop) {
    if (role == RoleEnum.TEACHER) {
      return true;
    }

    UserProgress progress = getProgressOrThrow(userId);

    return stop.getDisplayOrder() <= progress.getCurrentStop().getDisplayOrder();
  }

  /**
   * Determines whether the given user can access the given level.
   *
   * <p>Teachers always have access. For students, levels in previously completed stops are
   * accessible (replay), and levels in the current stop are accessible up to and including
   * the student's {@link UserProgress#getCurrentLevelOrder()}. The rest is locked.
   *
   * @param userId the user whose access is being checked
   * @param role the user's role
   * @param level the level being checked
   * @return {@code true} if the user can access the level, {@code false} otherwise
   * @throws UserProgressNotFoundException if the user is a student with no progress row
   */
  public boolean isLevelAccessible(UUID userId, RoleEnum role, Level level) {
    if (role == RoleEnum.TEACHER) {
      return true;
    }

    UserProgress progress = getProgressOrThrow(userId);
    Stop levelStop = level.getStop();
    Stop currentStop = progress.getCurrentStop();

    if (levelStop.getDisplayOrder() < currentStop.getDisplayOrder()) {
      return true;
    }
    if (levelStop.getDisplayOrder() > currentStop.getDisplayOrder()) {
      return false;
    }

    return level.getOrder() <= progress.getCurrentLevelOrder();
  }

  /**
   * Records that the given user completed the given level, advancing their
   * progress (as long as they are not replaying the level).
   *
   * <p>This method only advances the progress state when the completed level is exactly
   * the student's current level. Replays of earlier levels and out-of-order submissions
   * are silently ignored.
   *
   * <p>Advancement rules:
   * <ol>
   *   <li>If more levels remain in the current stop, increment
   *       {@link UserProgress#getCurrentLevelOrder()}</li>
   *   <li>Otherwise, if a next stop exists, move to its first level</li>
   *   <li>Otherwise, mark the game as completed</li>
   * </ol>
   *
   * @param userId the user who completed the level
   * @param role the user's role
   * @param level the level that was completed
   * @throws UserProgressNotFoundException if the user is a student with no progress row
   */
  @Transactional
  public boolean recordCompletionOfLevel(UUID userId, RoleEnum role, Level level) {
    if (role == RoleEnum.TEACHER) return false;

    UserProgress progress = getProgressOrThrow(userId);

    if (!shouldAdvance(progress, level)) return false;

    Stop completedStop = progress.getCurrentStop();
    advanceToNextLevel(progress);

    if (progress.isGameCompleted()){
      medalionService.awardInternalUsage(userId, "platinum", "Du fanget skurken");
    } else if(progress.getCurrentStop().getId() != completedStop.getId()){
      awardStopCompletionMedal(userId, completedStop);
    }

    return true;
  }

  /**
   * Performs the state transition for advancing a student's progress.
   *
   * <p>Caller must have verified via {@link #shouldAdvance} that advancement
   * is appropriate. This method does not re-check guards.
   */
  private void advanceToNextLevel(UserProgress progress) {
    Stop currentStop = progress.getCurrentStop();
    long levelsInStop = levelRepository.countByStopId(currentStop.getId());

    if (progress.getCurrentLevelOrder() < levelsInStop) {
      progress.setCurrentLevelOrder(progress.getCurrentLevelOrder() + 1);
    } else {
      stopRepository
          .findNextStopAfter(
              progress.getCurrentStop().getDisplayOrder()
          ).ifPresentOrElse(
              nextStop -> {
                progress.setCurrentStop(nextStop);
                progress.setCurrentLevelOrder(1);
              },
              () -> progress.setGameCompleted(true)
          );
    }
  }

  private void awardStopCompletionMedal(UUID userId, Stop completedStop){
    var medal = switch(completedStop.getName()){
      case "Nyhetskvartalet" -> Map.entry("Breaking news", "Du avslørte alle falske nyheter");
      case "Fotografen" -> Map.entry("Fanget på kamera", "Du gjennomskuet bildene!");
      case "Postkontoret" -> Map.entry("Ikke klikk!", "Du gjennomskuet falske eposter");
      case "Passordbanken" -> Map.entry("Passordvokter", "Du bygde passord som holder skurker ute!");
      case "Markedsplassen" -> Map.entry  ("God Handel", "Du var varsom i bazaaren");
      case "Den Sosiale Møteplassen" -> Map.entry("Chat chat chat", "Du var føre var på medier");
      default -> null;
    };
    if(medal != null){
      medalionService.awardInternalUsage(userId, medal.getKey(), medal.getValue());
    }
  }

  public Map<UUID, ProgressSummaryResponse> getProgressForStudents(Collection<UUID> userIds) {
    List<StopLevelCount> stopLevelCounts = stopRepository.findAllWithLevelCounts();
    int totalLevelsInGame = stopLevelCounts.stream()
        .mapToInt(s -> (int) s.getLevelCount())
        .sum();

    return userProgressRepository.findByUserIdIn(userIds).stream()
        .collect(Collectors.toMap(
            UserProgress::getId,
            progress -> progressMapper.toSummary(progress, stopLevelCounts, totalLevelsInGame)
        ));
  }

  public boolean boosFightUnlocked(UUID userId) {
    return getProgressOrThrow(userId).getCurrentStop().getDisplayOrder() == 7;
  }

  /**
   * Returns whether the student's progress should advance in response to
   * completing the given level.
   *
   * <p>Returns {@code false} for replays of completed levels
   */
  private boolean shouldAdvance(UserProgress progress, Level level) {
    if (progress.isGameCompleted()) return false;

    Stop currentStop = progress.getCurrentStop();
    return level.getStop().getId() == currentStop.getId()
        && level.getOrder() == progress.getCurrentLevelOrder();
  }

  private UserProgress getProgressOrThrow(UUID userId) {
    return userProgressRepository.findById(userId)
        .orElseThrow(() -> new UserProgressNotFoundException(userId));
  }
}