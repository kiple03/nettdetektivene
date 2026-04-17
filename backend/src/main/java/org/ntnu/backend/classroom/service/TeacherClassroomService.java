package org.ntnu.backend.classroom.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import org.ntnu.backend.game.repository.LevelCompletionRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.mystery.repository.MysterySubmissionRepository;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class TeacherClassroomService {

    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomService classroomService;
    private final ClassroomAuthorizationService classroomAuthorizationService;
    private final EnrollmentRepository enrollmentRepository;
    private final LevelCompletionRepository levelCompletionRepository;
    private final MysterySubmissionRepository mysterySubmissionRepository;
    private final UserRepository userRepository;
    private final StopRepository stopRepository;

    public ClassroomResponse createClassroom(CreateClassroomRequest request, UUID teacherId) {
        return classroomService.createClassroom(request, teacherId);
    }

    public List<ClassroomSummaryResponse> listClassrooms(UUID teacherId) {
        return classroomTeacherRepository
            .findClassroomsWithStudentCountByTeacherId(teacherId)
            .stream()
            .map(row -> ClassroomSummaryResponse.builder()
                .id(row[0].toString())
                .title((String) row[1])
                .description((String) row[2])
                .joinCode((String) row[3])
                .createdAt((LocalDateTime) row[4])
                .studentCount(((Number) row[5]).longValue())
                .build())
            .toList();
    }

    public ClassroomResponse updateClassroom(UUID classroomId, UpdateClassroomRequest request, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return classroomService.updateClassroom(classroomId, request);
    }

    @Transactional(readOnly = true)
    public List<StudentProgressResponse> listStudents(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return enrollmentRepository.findByClassroomIdAndLeftAtIsNull(classroomId)
            .stream()
            .map(enrollment -> {
                long count = levelCompletionRepository.countByEnrollmentId(enrollment.getId());
                return StudentProgressResponse.builder()
                    .userId(enrollment.getUser().getId().toString())
                    .username(enrollment.getUser().getUsername())
                    .levelsCompleted(count)
                    .medalsEarned(count)
                    .build();
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetail(UUID classroomId, UUID studentId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        var enrollment = enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var completions = levelCompletionRepository.findByEnrollmentId(enrollment.getId())
            .stream()
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
