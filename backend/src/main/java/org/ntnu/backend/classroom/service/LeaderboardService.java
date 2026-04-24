package org.ntnu.backend.classroom.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopLevelCount;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LeaderboardService {

  private final EnrollmentRepository enrollmentRepository;
  private final UserProgressRepository userProgressRepository;
  private final StopRepository stopRepository;

  public List<LeaderboardClassroomResponse> getGlobalLeaderboard() {
    Map<Integer, Long> levelsByDisplayOrder = stopRepository.findAllWithLevelCounts().stream()
        .collect(Collectors.toMap(StopLevelCount::getDisplayOrder, StopLevelCount::getLevelCount));

    List<Enrollment> enrollments = enrollmentRepository.findAllByLeftAtIsNull();
    List<UserProgress> userProgresses = userProgressRepository.findAllById(
        enrollments.stream().map(e -> e.getUser().getId()).toList()
    );

    Map<Classroom, List<Integer>> scoresByClassroom = new HashMap<>();
    Map<UUID, Integer> progressById = userProgresses.stream()
        .collect(Collectors.toMap(
            UserProgress::getId,
            up -> calculateProgressScore(up, levelsByDisplayOrder)
        ));

    for (Enrollment e : enrollments) {
      int score = progressById.get(e.getUser().getId());
      scoresByClassroom.computeIfAbsent(e.getClassroom(), k -> new ArrayList<>()).add(score);
    }

    return;
  }

  private int calculateProgressScore(UserProgress up, Map<Integer, Long> levelsByDisplayOrder) {
    if (up.isGameCompleted()) {
      return (int) (levelsByDisplayOrder.values().stream().mapToLong(l -> l).sum() * 100);
    }
    int completed = levelsByDisplayOrder.entrySet().stream()
        .filter(e -> e.getKey() < up.getCurrentStop().getDisplayOrder())
        .mapToInt(e -> e.getValue().intValue())
        .sum();
    completed += (up.getCurrentLevelOrder() - 1);
    return completed * 100;
  }
}
