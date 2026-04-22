package org.ntnu.backend.classroom.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.LevelCompletionEntry;
import org.ntnu.backend.classroom.dto.MysterySubmissionResponse;
import org.ntnu.backend.classroom.dto.ReviewMysteryRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.dto.progress.ProgressSummaryResponse;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.service.ProgressService;
import org.ntnu.backend.mystery.repository.MysterySubmissionRepository;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class handling teacher-specific views, dashboards, and authorization
 * for managing a classroom. It delegates core operations to ClassroomService.
 */
@Service
@AllArgsConstructor
public class TeacherClassroomService {

    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomService classroomService;
    private final ClassroomAuthorizationService classroomAuthorizationService;
    private final EnrollmentRepository enrollmentRepository;
    private final LevelRepository levelRepository;
    private final MysterySubmissionRepository mysterySubmissionRepository;
    private final UserRepository userRepository;
    private final StopRepository stopRepository;
    private final ProgressService progressService;

    public ClassroomResponse createClassroom(CreateClassroomRequest request, UUID teacherId) {
        return classroomService.createClassroom(request, teacherId);
    }


    /**
     * Lists all classrooms owned by a specific teacher, calculating overall
     * student progress percentage based on each active student's
     * {@link ProgressSummaryResponse#totalLevelsCompleted()}.
     *
     * @param teacherId the UUID of the teacher
     * @return a list of classroom summaries with calculated statistics
     */
    @Transactional(readOnly = true)
    public List<ClassroomSummaryResponse> listClassrooms(UUID teacherId) {
        List<Classroom> classrooms = classroomTeacherRepository.findClassroomsByTeacherId(teacherId);
        int totalLevelsInGame = (int) levelRepository.count();

        List<UUID> allStudentIds = classrooms.stream()
            .flatMap(c -> enrollmentRepository
                .findByClassroomIdAndLeftAtIsNull(c.getId()).stream())
            .map(e -> e.getUser().getId())
            .toList();

        Map<UUID, ProgressSummaryResponse> progressByStudent =
            progressService.getProgressForStudents(allStudentIds);

        return classrooms.stream()
            .map(classroom -> buildClassroomSummary(
                classroom, progressByStudent, totalLevelsInGame))
            .toList();
    }

    /**
     * Verifies teacher ownership, then updates the classroom details.
     *
     * @param classroomId the classroom to update
     * @param request the update data
     * @param principal the authenticated teacher context
     * @return the updated classroom details
     */
    public ClassroomResponse updateClassroom(UUID classroomId, UpdateClassroomRequest request, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return classroomService.updateClassroom(classroomId, request);
    }

    /**
     * Verifies teacher ownership, then completely deletes the classroom.
     *
     * @param classroomId the classroom to delete
     * @param principal the authenticated teacher context
     */
    public void deleteClassroom(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        classroomService.deleteClassroom(classroomId);
    }

    /**
     * Retrieves all active students in a specific classroom along with a count
     * of their completed levels/medals.
     *
     * @param classroomId the classroom to query
     * @param principal the authenticated teacher context
     * @return a list of student progress statistics
     */
    @Transactional(readOnly = true)
    public List<StudentProgressResponse> listStudents(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return enrollmentRepository.findByClassroomIdAndLeftAtIsNull(classroomId)
            .stream()
            .map(enrollment -> {
                return StudentProgressResponse.builder()
                    .userId(enrollment.getUser().getId().toString())
                    .username(enrollment.getUser().getUsername())
                    .levelsCompleted(count)
                    .medalsEarned(count)
                    .build();
            })
            .toList();
    }

    
    /**
     * Fetches detailed data for a specific student in a classroom, including
     * exactly which levels they have completed and when.
     *
     * @param classroomId the classroom ID
     * @param studentId the user ID of the student
     * @param principal the authenticated teacher context
     * @return an object detailing the student's level completions
     */
    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetail(UUID classroomId, UUID studentId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        var enrollment = enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            .map(lc -> LevelCompletionEntry.builder()
                .levelId(lc.getLevel().getId())
                .completedAt(lc.getCompletedAt())
                .build())
            .toList();
        return StudentDetailResponse.builder()
            .username(enrollment.getUser().getUsername())
            .completions(completions)
            .build();
    }

    /**
     * Verifies teacher access and retrieves basic metadata for a specific classroom.
     *
     * @param classroomId the classroom ID
     * @param principal the authenticated teacher context
     * @return the basic details of the classroom
     */
    public ClassroomResponse getClassroom(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ClassroomResponse.builder()
            .id(classroom.getId().toString())
            .title(classroom.getTitle())
            .description(classroom.getDescription())
            .joinCode(classroom.getJoinCode())
            .createdAt(classroom.getCreatedAt())
            .build();
    }

    /**
     * Retrieves a list of all mystery assignment submissions for a specific classroom.
     *
     * @param classroomId the classroom ID
     * @param principal the authenticated teacher context
     * @return a list containing the status and content of all mystery submissions
     */
    @Transactional(readOnly = true)
    public List<MysterySubmissionResponse> listMysterySubmissions(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return mysterySubmissionRepository.findByEnrollmentClassroomId(classroomId)
            .stream()
            .map(ms -> MysterySubmissionResponse.builder()
                .id(ms.getId())
                .studentUsername(ms.getEnrollment().getUser().getUsername())
                .content(ms.getContent())
                .imageUrl(ms.getImageUrl())
                .submittedAt(ms.getSubmittedAt())
                .status(ms.getStatus())
                .featured(ms.isFeatured())
                .build())
            .toList();
    }

    /**
     * Gathers a chronological list of recent student events (like level completions
     * or mystery submissions) across all classrooms owned by the teacher.
     *
     * @param teacherId the UUID of the teacher
     * @return a combined, time-sorted list of activity notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID teacherId) {
        List<NotificationResponse> notifications = new ArrayList<>();

        levelCompletionRepository.findRecentByTeacherId(teacherId)
            .forEach(lc -> {
                var enrollment = lc.getEnrollment();
                var username = enrollment.getUser().getUsername();
                var classroom = enrollment.getClassroom();
                notifications.add(NotificationResponse.builder()
                    .type("LEVEL_COMPLETED")
                    .message(username + " completed a level in " + classroom.getTitle())
                    .classroomId(classroom.getId().toString())
                    .studentUsername(username)
                    .timestamp(lc.getCompletedAt())
                    .build());
            });

        mysterySubmissionRepository.findRecentByTeacherId(teacherId)
            .forEach(ms -> {
                var enrollment = ms.getEnrollment();
                var username = enrollment.getUser().getUsername();
                var classroom = enrollment.getClassroom();
                notifications.add(NotificationResponse.builder()
                    .type("MYSTERY_SUBMISSION")
                    .message(username + " submitted a mystery answer in " + classroom.getTitle())
                    .classroomId(classroom.getId().toString())
                    .studentUsername(username)
                    .timestamp(ms.getSubmittedAt())
                    .build());
            });

        notifications.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return notifications;
    }

    /**
     * Verifies access and applies a teacher's review (grading or featuring) to a
     * student's mystery submission.
     *
     * @param classroomId the classroom ID
     * @param submissionId the specific submission being reviewed
     * @param request containing the new status and featured flag
     * @param principal the authenticated teacher context
     * @return the updated submission data
     */
    @Transactional
    public MysterySubmissionResponse reviewMysterySubmission(
            UUID classroomId,
            UUID submissionId,
            ReviewMysteryRequest request,
            JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        var submission = mysterySubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!submission.getEnrollment().getClassroom().getId().equals(classroomId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var teacher = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        if (request.getStatus() != null) {
            submission.setStatus(request.getStatus());
            submission.setReviewedByTeacher(teacher);
            submission.setReviewedAt(java.time.Instant.now());
        }
        if (request.getFeatured() != null) {
            submission.setFeatured(request.getFeatured());
        }
        var saved = mysterySubmissionRepository.save(submission);
        return MysterySubmissionResponse.builder()
            .id(saved.getId())
            .studentUsername(saved.getEnrollment().getUser().getUsername())
            .content(saved.getContent())
            .imageUrl(saved.getImageUrl())
            .submittedAt(saved.getSubmittedAt())
            .status(saved.getStatus())
            .featured(saved.isFeatured())
            .build();
    }

    /**
     * Retrieves the ordered list of all checkpoints ("stops") defined in the game map.
     *
     * @return an ordered list of StopResponses
     */
    @Transactional(readOnly = true)
    public List<StopResponse> listStops() {
        return stopRepository.findAllByOrderByDisplayOrderAsc()
            .stream()
            .map(stop -> StopResponse.builder()
                .id(stop.getId())
                .name(stop.getName())
                .displayOrder(stop.getDisplayOrder())
                .build())
            .toList();
    }
}
