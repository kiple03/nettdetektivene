package org.ntnu.backend.classroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedClassroomResponse;
import org.ntnu.backend.classroom.dto.leaderboard.RankedStudentResponse;
import org.ntnu.backend.classroom.service.LeaderboardService;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing leaderboard endpoints for classrooms and students.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/leaderboard")
@Tag(name = "Leaderboard", description = "Classroom and student rankings")
public class LeaderboardController {

  private final LeaderboardService leaderboardService;
  private final ClassroomAuthorizationService classroomAuthorizationService;

  /**
   * Returns a paginated global leaderboard of all classrooms ranked by score.
   *
   * @param page zero-based page index (default 0)
   * @param size number of entries per page, 1–100 (default 10)
   * @return paginated list of ranked classrooms
   */
  @GetMapping("/classrooms")
  @Operation(
      summary = "Get global classroom leaderboard",
      description = "Paginated ranking of all classrooms by score. Accessible to all authenticated users"
  )
  @ApiResponse(responseCode = "200", description = "Paginated leaderboard returned")
  public Page<RankedClassroomResponse> getGlobalLeaderboard(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
  ) {
    return leaderboardService.getGlobalLeaderboard(PageRequest.of(page, size));
  }

  /**
   * Returns the ranked student leaderboard for a specific classroom.
   * Only accessible by a teacher who has access to the given classroom.
   *
   * @param classroomId the classroom whose student ranking is requested
   * @param principal   the authenticated teacher
   * @return list of students ranked by score within the classroom
   * @throws org.springframework.security.access.AccessDeniedException if the teacher lacks access to the classroom
   */
  @GetMapping("/classrooms/{classroomId}")
  @PreAuthorize("hasRole('TEACHER')")
  @Operation(
      summary = "Get student leaderboard for a classroom",
      description = "Returns students ranked by score. Only accessible by a teacher with access to the classroom"
  )
  public List<RankedStudentResponse> getClassroomLeaderboard(
      @PathVariable UUID classroomId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
  ) {
    classroomAuthorizationService.verifyAccess(principal, classroomId);
    return leaderboardService.getRankedStudentLeaderboard(classroomId);
  }

  /**
   * Returns leaderboard scores for all classrooms belonging to a teacher.
   * Only accessible by teachers.
   *
   * @param teacherId the UUID of the teacher whose classrooms are queried
   * @param principal the authenticated teacher
   * @return list of classroom leaderboard entries for the given teacher
   */
  @GetMapping("teachers/{teacherId}")
  @PreAuthorize("hasRole('TEACHER')")
  @Operation(
      summary = "Get leaderboard scores for a teacher's classrooms",
      description = "Returns score summaries for all classrooms owned by the given teacher"
  )
  public List<LeaderboardClassroomResponse> getTeacherClassroomScores(
      @PathVariable UUID teacherId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
  ) {
    return leaderboardService.getTeacherClassroomScores(teacherId, principal.getUserId());
  }
}