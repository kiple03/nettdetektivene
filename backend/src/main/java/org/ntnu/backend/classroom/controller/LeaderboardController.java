package org.ntnu.backend.classroom.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedStudentResponse;
import org.ntnu.backend.classroom.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/leaderboard")
public class LeaderboardController {

  private final LeaderboardService leaderboardService;

  @GetMapping("/classrooms")
  public List<RankedClassroomResponse> getGlobalLeaderboard() {
    return leaderboardService.getGlobalLeaderboard();
  }

  @GetMapping("/classrooms/{classroomId}")
  public List<RankedStudentResponse> getClassroomLeaderboard(
      @PathVariable UUID classroomId
  ) {
    // TODO: check user in classroom
    return leaderboardService.getRankedStudentLeaderboard(classroomId);
  }

}
