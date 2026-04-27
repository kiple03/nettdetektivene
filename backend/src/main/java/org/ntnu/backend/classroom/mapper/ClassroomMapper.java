package org.ntnu.backend.classroom.mapper;

import java.util.List;

import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.game.dto.progress.ProgressSummaryResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper Component. It translates data between database models and DTO's.
 */
@Component
public class ClassroomMapper {

  /**
   * Mapper method that takes raw ClassRoom object and converts them to 
   * ClassroomResponse JSON objects for frontend.
   * @param c classroom
   * @return friendly classroom object for frontend
   */
  public ClassroomResponse toResponse(Classroom c) {
    return ClassroomResponse.builder()
        .id(c.getId().toString())
        .title(c.getTitle())
        .description(c.getDescription())
        .joinCode(c.getJoinCode())
        .build();
  }

  /**
   * Mapper method that converts JSON payload into Classroom for database 
   * @param request JSON payload
   * @param joinCode code 
   * @return friendly classroom object for database
   */
  public Classroom toEntity(CreateClassroomRequest request, String joinCode) {
    return Classroom.builder()
        .title(request.getTitle().trim())
        .description(request.getDescription() != null ? request.getDescription().trim() : null)
        .joinCode(joinCode)
        .build();
  }

  /**
   * Mapper method that updates database using classroom objects in database
   * @param classroom instance
   * @param request request
   */
  public void applyUpdates(Classroom classroom, UpdateClassroomRequest request) {
    if (request.getTitle() != null && !request.getTitle().isBlank()) {
      classroom.setTitle(request.getTitle().trim());
    }
    if (request.getDescription() != null) {
      classroom.setDescription(
          request.getDescription().isBlank() ? null : request.getDescription().trim());
    }
  }

  /**
   * Builds a classroom summary with computed progress statistics for the teacher dashboard.
   *
   * @param classroom the classroom being summarized
   * @param studentCount number of active enrollments
   * @param totalLevelsInGame total levels across all stops (denominator for the percent)
   * @param completedCount sum of totalLevelsCompleted across all active students
   * @return the summary DTO with computed progress percentage
   */
  public ClassroomSummaryResponse toSummary(
      Classroom classroom,
      long studentCount,
      int totalLevelsInGame,
      int completedCount,
      int avgScore
  ) {

    long possible = (long) totalLevelsInGame * studentCount;
    int progressPercent = possible > 0 ? (int) (completedCount * 100L / possible) : 0;

    return ClassroomSummaryResponse.builder()
        .id(classroom.getId().toString())
        .title(classroom.getTitle())
        .description(classroom.getDescription())
        .joinCode(classroom.getJoinCode())
        .createdAt(classroom.getCreatedAt())
        .studentCount(studentCount)
        .assignmentCount(totalLevelsInGame)
        .completedCount(completedCount)
        .avgScore(avgScore)
        .progressPercent(progressPercent)
        .build();
  }

  /**
   * Maps an active enrollment plus the student's progress into a dashboard list row.
   *
   * @param enrollment the active enrollment
   * @param progress the student's progress summary, or {@code null} if missing
   */
  public StudentProgressResponse toStudentProgress(
    Enrollment enrollment,
    ProgressSummaryResponse progress,
    int score,
    int medalsEarned,
    List<EquippedItemDto> equippedItems) {

    int levelsCompleted = progress != null ? progress.totalLevelsCompleted() : 0;
    return StudentProgressResponse.builder()
        .userId(enrollment.getUser().getId().toString())
        .username(enrollment.getUser().getUsername())
        .levelsCompleted(levelsCompleted)
        .score(score)
        .medalsEarned(medalsEarned)
        .equippedItems(equippedItems)
        .build();
  }

  /**
   * Maps an active enrollment plus the student's progress into the detailed view.
   *
   * @param enrollment the active enrollment
   * @param progress the student's progress summary, or {@code null} if missing
   */
  public StudentDetailResponse toStudentDetail(
      Enrollment enrollment, ProgressSummaryResponse progress) {

    StudentDetailResponse.StudentDetailResponseBuilder builder = StudentDetailResponse.builder()
        .username(enrollment.getUser().getUsername())
        .email(enrollment.getUser().getEmail())
        .enrolledAt(enrollment.getEnrolledAt());

    if (progress != null) {
      builder
          .currentStopName(progress.currentStopName())
          .currentLevelOrder(progress.currentLevelOrder())
          .totalLevelsCompleted(progress.totalLevelsCompleted())
          .gameCompleted(progress.gameCompleted());
    }

    return builder.build();
  }
}
