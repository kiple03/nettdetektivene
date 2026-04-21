package org.ntnu.backend.game.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.exception.UserProgressNotFoundException;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProgressService {

  private final UserProgressRepository userProgressRepository;
  private final LevelRepository levelRepository;
  private final StopRepository stopRepository;

  public void initProgressForStudent(User user) {
    Stop firstStop =  stopRepository.findFirstStop()
        .orElseThrow(() -> new IllegalStateException("First stop not configured"));

    UserProgress progress = UserProgress.createInitial(user, firstStop);

    userProgressRepository.save(progress);
  }

  public boolean isStopAccessible(UUID userId, RoleEnum role, Stop stop) {
    if (role == RoleEnum.TEACHER) {
      return true;
    }

    UserProgress progress = getProgressOrThrow(userId);

    return stop.getDisplayOrder() <= progress.getCurrentStop().getDisplayOrder();
  }


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

  @Transactional
  public void recordCompletionOfLevel(UUID userId, RoleEnum role, Level level) {
    if (role == RoleEnum.TEACHER) return;

    UserProgress progress = getProgressOrThrow(userId);

    if (!shouldAdvance(progress, level)) return;

    advanceToNextLevel(progress);
  }


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
