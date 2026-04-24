package org.ntnu.backend.classroom.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.mapper.LeaderboardMapper;
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

  private final LeaderboardMapper leaderboardMapper;


  public List<RankedClassroomResponse> getGlobalLeaderboard() {
    Map<Integer, Long> levelsByDisplayOrder = stopRepository.findAllWithLevelCounts().stream()
        .collect(Collectors.toMap(StopLevelCount::getDisplayOrder, StopLevelCount::getLevelCount));

    List<Enrollment> enrollments = enrollmentRepository.findAllByLeftAtIsNull();
    List<UserProgress> userProgresses = userProgressRepository.findAllById(
        enrollments.stream().map(e -> e.getUser().getId()).toList()
    );

    Map<Classroom, List<Integer>> scoresByClassroom = groupScoresByClassroom(
        userProgresses, levelsByDisplayOrder, enrollments);

    List<LeaderboardClassroomResponse> sorted = buildSortedLeaderboard(
        scoresByClassroom);

    return leaderboardMapper.toRankedList(sorted);
  }

  private List<LeaderboardClassroomResponse> buildSortedLeaderboard(
      Map<Classroom, List<Integer>> scoresByClassroom
  ) {
    return scoresByClassroom.entrySet().stream()
        .map(e -> new LeaderboardClassroomResponse(
            e.getKey().getTitle(),
            e.getValue().stream().mapToInt(Integer::intValue).sum(),
            (int) e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0)
        ))
        .sorted(Comparator.comparingInt(LeaderboardClassroomResponse::avgScore).reversed())
        .toList();
  }

  private Map<Classroom, List<Integer>> groupScoresByClassroom(
      List<UserProgress> userProgresses,
      Map<Integer, Long> levelsByDisplayOrder,
      List<Enrollment> enrollments
  ) {
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
    return scoresByClassroom;
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
