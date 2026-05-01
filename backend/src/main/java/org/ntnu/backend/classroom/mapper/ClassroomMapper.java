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
 * Translates between {@link Classroom}/{@link Enrollment} entities and their DTO representations.
 */
@Component
public class ClassroomMapper {

  /**
   * Maps a {@link Classroom} entity to a {@link ClassroomResponse} DTO.
   *
   * @param c the classroom entity
   * @return DTO suitable for API responses
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
   * Creates a new {@link Classroom} entity from a creation request.
   *
   * @param request the incoming creation payload
   * @param joinCode the generated join code to assign to the classroom
   * @return a new, unpersisted {@link Classroom} entity
   */
  public Classroom toEntity(CreateClassroomRequest request, String joinCode) {
    return Classroom.builder()
        .title(request.getTitle().trim())
        .description(request.getDescription() != null ? request.getDescription().trim() : null)
        .joinCode(joinCode)
        .build();
  }

  /**
   * Applies non-null fields from an update request onto an existing {@link Classroom} entity in place.
   * Blank strings are normalised to {@code null} for the description field.
   *
   * @param classroom the entity to mutate
   * @param request   the update payload; fields that are {@code null} are left unchanged
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
   * Builds a {@link ClassroomSummaryResponse} with computed progress statistics for the teacher dashboard.
   *
   * @param classroom       the classroom being summarised
   * @param studentCount    number of active enrollments
   * @param totalStops      total number of stops in the game (denominator for progress percent)
   * @param avgStopPosition average stop position reached across all active students
   * @param avgScore        average score across all active students
   * @return summary DTO with a computed {@code progressPercent}
   */
  public ClassroomSummaryResponse toSummary(
      Classroom classroom,
      long studentCount,
      int totalStops,
      double avgStopPosition,
      int avgScore
  ) {
    int progressPercent = totalStops > 0 ? (int) Math.round(avgStopPosition * 100.0 / totalStops) : 0;

    return ClassroomSummaryResponse.builder()
        .id(classroom.getId().toString())
        .title(classroom.getTitle())
        .description(classroom.getDescription())
        .joinCode(classroom.getJoinCode())
        .createdAt(classroom.getCreatedAt())
        .studentCount(studentCount)
        .totalStops(totalStops)
        .avgStopPosition(avgStopPosition)
        .avgScore(avgScore)
        .progressPercent(progressPercent)
        .build();
  }

  /**
   * Maps an active enrollment plus aggregated stats into a {@link StudentProgressResponse} for
   * the classroom student-list view.
   *
   * @param enrollment    the active enrollment (carries user details)
   * @param progress      the student's progress summary, or {@code null} if none exists yet
   * @param score         the student's total score
   * @param medalsEarned  number of medals the student has earned
   * @param totalStops    total stops in the game (used for progress display)
   * @param equippedItems the avatar items currently equipped by the student
   * @return a {@link StudentProgressResponse} for the teacher dashboard row
   */
  public StudentProgressResponse toStudentProgress(
    Enrollment enrollment,
    ProgressSummaryResponse progress,
    int score,
    int medalsEarned,
    int totalStops,
    List<EquippedItemDto> equippedItems) {

    int currentStopDisplayOrder = progress != null ? progress.currentStopDisplayOrder() : 0;
    return StudentProgressResponse.builder()
        .userId(enrollment.getUser().getId().toString())
        .username(enrollment.getUser().getUsername())
        .email(enrollment.getUser().getEmail())
        .currentStopDisplayOrder(currentStopDisplayOrder)
        .totalStops(totalStops)
        .score(score)
        .medalsEarned(medalsEarned)
        .equippedItems(equippedItems)
        .hairColor(enrollment.getUser().getHairColor())
        .build();
  }

  /**
   * Maps an enrollment and progress snapshot into a {@link StudentDetailResponse} for the
   * per-student detail page.
   *
   * @param enrollment the active enrollment (carries user and enrolment date)
   * @param progress   the student's progress summary, or {@code null} if the student has not started
   * @return a {@link StudentDetailResponse}; progress-related fields are omitted when {@code progress} is {@code null}
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
