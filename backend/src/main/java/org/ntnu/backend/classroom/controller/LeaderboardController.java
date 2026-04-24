package org.ntnu.backend.classroom.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
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

}
