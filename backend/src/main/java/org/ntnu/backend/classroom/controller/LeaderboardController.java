package org.ntnu.backend.classroom.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedStudentResponse;
import org.ntnu.backend.classroom.service.LeaderboardService;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/leaderboard")
public class LeaderboardController {

  private final LeaderboardService leaderboardService;
  private final ClassroomAuthorizationService classroomAuthorizationService;

  @GetMapping("/classrooms")
  public List<RankedClassroomResponse> getGlobalLeaderboard() {
    return leaderboardService.getGlobalLeaderboard();
  }

  @GetMapping("/classrooms/{classroomId}")
  @PreAuthorize("hasRole('TEACHER')")
  public List<RankedStudentResponse> getClassroomLeaderboard(
      @PathVariable UUID classroomId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
  ) {
    classroomAuthorizationService.verifyAccess(principal, classroomId);
    return leaderboardService.getRankedStudentLeaderboard(classroomId);
  }

  @GetMapping("teachers/{teacherId}")
  @PreAuthorize("hasRole('TEACHER')")
  public List<LeaderboardClassroomResponse> getTeacherClassroomScores(
      @PathVariable UUID teacherId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
  ) {
    // verify access
    return leaderboardService.getTeacherClassroomScores(teacherId);
  }

}
