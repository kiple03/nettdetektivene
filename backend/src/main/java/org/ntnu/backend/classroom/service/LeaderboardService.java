package org.ntnu.backend.classroom.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardStudentResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedStudentResponse;
import org.ntnu.backend.classroom.mapper.LeaderboardMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.StopLevelCount;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LeaderboardService {

  private final EnrollmentRepository enrollmentRepository;
  private final UserProgressRepository userProgressRepository;
  private final StopRepository stopRepository;
  private final LeaderboardMapper leaderboardMapper;
  private final ClassroomTeacherRepository classroomTeacherRepository;

  public List<RankedStudentResponse> getRankedStudentLeaderboard(UUID classroomId) {
    List<Enrollment> enrollments = enrollmentRepository.findAllByClassroomIdAndLeftAtIsNull(classroomId);
    Map<UUID, Integer> scores = computeScores(enrollments);

    List<LeaderboardStudentResponse> sorted = enrollments.stream()
        .map(e -> new LeaderboardStudentResponse(
            e.getUser().getUsername(),
            scores.getOrDefault(e.getUser().getId(), 0)
        ))
        .sorted(Comparator.comparingInt(LeaderboardStudentResponse::score).reversed())
        .toList();

    return leaderboardMapper.toRankedStudentList(sorted);
  }

  public List<LeaderboardClassroomResponse> getTeacherClassroomScores(UUID teacherId) {
    List<UUID> classroomIds = classroomTeacherRepository.findClassroomsByTeacherId(teacherId)
        .stream().map(Classroom::getId).toList();

    List<Enrollment> enrollments = enrollmentRepository.findAllByClassroomIdInAndLeftAtIsNull(classroomIds);
    Map<UUID, Integer> scores = computeScores(enrollments);
    Map<Classroom, List<Integer>> byClassroom = groupScoresByClassroom(enrollments, scores);
    return buildSortedLeaderboard(byClassroom);
  }

  public Page<RankedClassroomResponse> getGlobalLeaderboard(Pageable pageable) {
    List<RankedClassroomResponse> allClassroomsRanked = buildGlobalLeaderboard();
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allClassroomsRanked.size());
    List<RankedClassroomResponse> page = start >= allClassroomsRanked.size()
        ? List.of()
        : allClassroomsRanked.subList(start, end);
    return new PageImpl<>(page, pageable, allClassroomsRanked.size());
  }

  private List<RankedClassroomResponse> buildGlobalLeaderboard() {
    List<Enrollment> enrollments = enrollmentRepository.findAllByLeftAtIsNull();
    Map<UUID, Integer> scores = computeScores(enrollments);
    Map<Classroom, List<Integer>> byClassroom = groupScoresByClassroom(enrollments, scores);
    List<LeaderboardClassroomResponse> sorted = buildSortedLeaderboard(byClassroom);
    return leaderboardMapper.toRankedList(sorted);
  }

  public Map<UUID, Integer> computeScores(List<Enrollment> enrollments) {
    Map<Integer, Long> levelsPerStop = stopRepository.findAllWithLevelCounts().stream()
        .collect(Collectors.toMap(StopLevelCount::getDisplayOrder, StopLevelCount::getLevelCount));

    return userProgressRepository.findAllById(
        enrollments.stream().map(e -> e.getUser().getId()).toList()
    ).stream().collect(Collectors.toMap(
        UserProgress::getId,
        up -> calculateProgressScore(up, levelsPerStop)
    ));
  }

  private Map<Classroom, List<Integer>> groupScoresByClassroom(
      List<Enrollment> enrollments, Map<UUID, Integer> scores
  ) {
    Map<Classroom, List<Integer>> result = new HashMap<>();
    for (Enrollment e : enrollments) {
      int score = scores.getOrDefault(e.getUser().getId(), 0);
      result.computeIfAbsent(e.getClassroom(), k -> new ArrayList<>()).add(score);
    }
    return result;
  }

  private List<LeaderboardClassroomResponse> buildSortedLeaderboard(
      Map<Classroom, List<Integer>> scoresByClassroom
  ) {
    return scoresByClassroom.entrySet().stream()
        .map(e -> new LeaderboardClassroomResponse(
            e.getKey().getId().toString(),
            e.getKey().getTitle(),
            e.getValue().stream().mapToInt(Integer::intValue).sum(),
            (int) e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0)
        ))
        .sorted(Comparator.comparingInt(LeaderboardClassroomResponse::avgScore).reversed())
        .toList();
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
