package org.ntnu.backend.game.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.exception.UserProgressNotFoundException;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProgressService {

  private final UserProgressRepository userProgressRepository;
  private final StopRepository stopRepository;

  public void initProgressForStudent(User user) {
    Stop firstStop =  stopRepository.findFirstByOrderByDisplayOrderAsc()
        .orElseThrow(() -> new IllegalStateException("First stop not configured"));

    UserProgress progress = UserProgress.createInitial(user, firstStop);

    userProgressRepository.save(progress);
  }

  public boolean isStopAccessible(UUID userId, RoleEnum role, Stop stop) {
    if (role == RoleEnum.TEACHER) {
      return true;
    }

    UserProgress progress = userProgressRepository.findById(userId)
        .orElseThrow(() -> new UserProgressNotFoundException(userId));

    return stop.getDisplayOrder() <= progress.getCurrentStop().getDisplayOrder();
  }

  public boolean isLevelAccessible(UUID userId, RoleEnum role, Level level) {
    if (role == RoleEnum.TEACHER) {
      return true;
    }

    UserProgress progress = userProgressRepository.findById(userId)
        .orElseThrow(() -> new UserProgressNotFoundException(userId));

    Stop levelStop = level.getStop();
    Stop currentStop = progress.getCurrentStop();

    if (levelStop.getDisplayOrder() < currentStop.getDisplayOrder()) {
      return true;
    }
    if (levelStop.getDisplayOrder() > currentStop.getDisplayOrder()) {
      return false;
    }

    return level.getOrder() <= progress.getLevelsCompletedInCurrentStop() + 1;
  }
}
