package org.ntnu.backend.classroom.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CoTeacherResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.InviteCoTeacherRequest;
import org.ntnu.backend.classroom.dto.LevelCompletionEntry;
import org.ntnu.backend.classroom.dto.MysterySubmissionResponse;
import org.ntnu.backend.classroom.dto.ReviewMysteryRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateStudentUsernameRequest;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.security.access.AccessDeniedException;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.repository.LevelCompletionRepository;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
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
    private final LevelCompletionRepository levelCompletionRepository;
    private final LevelRepository levelRepository;
    private final MysterySubmissionRepository mysterySubmissionRepository;
    private final UserRepository userRepository;
    private final UserInventoryItemRepository userInventoryItemRepository;

    public ClassroomResponse createClassroom(CreateClassroomRequest request, UUID teacherId) {
        return classroomService.createClassroom(request, teacherId);
    }

    /**
     * Lists all classrooms owned by a specific teacher, calculating overall
     * student progress percentage and assignment completion statistics.
     *
     * @param teacherId the UUID of the teacher
     * @return a list of classroom summaries with calculated statistics
     */
    public List<ClassroomSummaryResponse> listClassrooms(UUID teacherId) {
        long assignmentCount = levelRepository.count();
        List<Object[]> rows = classroomTeacherRepository.findClassroomsWithStudentCountByTeacherId(teacherId);

        List<UUID> classroomIds = rows.stream()
            .map(row -> UUID.fromString(row[0].toString()))
            .toList();

        Map<UUID, List<String>> teachersByClassroom = classroomTeacherRepository
            .findWithUsersByClassroomIdIn(classroomIds)
            .stream()
            .collect(Collectors.groupingBy(
                ct -> ct.getId().getClassroomId(),
                Collectors.mapping(ct -> ct.getUser().getUsername(), Collectors.toList())
            ));

        return rows.stream()
            .map(row -> {
                UUID classroomId = UUID.fromString(row[0].toString());
                long studentCount = ((Number) row[5]).longValue();
                long completedCount = ((Number) row[6]).longValue();
                long possible = assignmentCount * studentCount;
                int progressPercent = possible > 0 ? (int) (completedCount * 100 / possible) : 0;
                return ClassroomSummaryResponse.builder()
                    .id(classroomId.toString())
                    .title((String) row[1])
                    .description((String) row[2])
                    .joinCode((String) row[3])
                    .createdAt((LocalDateTime) row[4])
                    .studentCount(studentCount)
                    .assignmentCount((int) assignmentCount)
                    .completedCount((int) completedCount)
                    .progressPercent(progressPercent)
                    .teacherUsernames(teachersByClassroom.getOrDefault(classroomId, List.of()))
                    .build();
            })
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
                long count = levelCompletionRepository.countByEnrollmentId(enrollment.getId());
                UUID userId = enrollment.getUser().getId();
                List<EquippedItemDto> equippedItems = userInventoryItemRepository
                    .findByUserIdAndIsEquippedTrue(userId)
                    .stream()
                    .map(inv -> EquippedItemDto.builder()
                        .id(inv.getAvatarItem().getId())
                        .name(inv.getAvatarItem().getName())
                        .category(inv.getAvatarItem().getCategory())
                        .imageUrl("/assets/" + inv.getAvatarItem().getName() + ".svg")
                        .build())
                    .toList();
                return StudentProgressResponse.builder()
                    .userId(userId.toString())
                    .username(enrollment.getUser().getUsername())
                    .levelsCompleted(count)
                    .medalsEarned(count)
                    .equippedItems(equippedItems)
                    .build();
            })
            .toList();
    }

    @Transactional
    public void removeStudent(UUID classroomId, UUID studentId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        var enrollment = enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        enrollment.setLeftAt(Instant.now());
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void updateStudentUsername(UUID classroomId, UUID studentId, UpdateStudentUsernameRequest request, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var user = userRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String newUsername = request.username().trim();
        if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brukernavnet er allerede tatt");
        }
        user.setUsername(newUsername);
        userRepository.save(user);
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

    public List<CoTeacherResponse> listCoTeachers(UUID classroomId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        return classroomTeacherRepository.findWithUsersByClassroomId(classroomId)
            .stream()
            .map(ct -> CoTeacherResponse.builder()
                .userId(ct.getUser().getId().toString())
                .username(ct.getUser().getUsername())
                .email(ct.getUser().getEmail())
                .owner(ct.isOwner())
                .build())
            .toList();
    }

    @Transactional
    public CoTeacherResponse inviteCoTeacher(UUID classroomId, InviteCoTeacherRequest request, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        if (!classroomTeacherRepository.isOwnerOfClassroom(classroomId, principal.getUserId())) {
            throw new AccessDeniedException("Bare eier kan invitere lærere");
        }
        var invitee = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingen lærer med den e-posten ble funnet"));
        if (invitee.getRole() != RoleEnum.TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brukeren er ikke en lærer");
        }
        if (classroomTeacherRepository.existsByClassroomIdAndUserId(classroomId, invitee.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Læreren er allerede lagt til i klassen");
        }
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        classroomTeacherRepository.save(ClassroomTeacher.builder()
            .id(new ClassroomTeacherId(classroomId, invitee.getId()))
            .classroom(classroom)
            .user(invitee)
            .isOwner(false)
            .build());
        return CoTeacherResponse.builder()
            .userId(invitee.getId().toString())
            .username(invitee.getUsername())
            .email(invitee.getEmail())
            .owner(false)
            .build();
    }

    @Transactional
    public void removeCoTeacher(UUID classroomId, UUID targetUserId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        UUID requesterId = principal.getUserId();
        boolean requesterIsOwner = classroomTeacherRepository.isOwnerOfClassroom(classroomId, requesterId);
        boolean removingSelf = requesterId.equals(targetUserId);
        if (!requesterIsOwner && !removingSelf) {
            throw new AccessDeniedException("Bare eier kan fjerne andre lærere");
        }
        if (requesterIsOwner && removingSelf) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eier kan ikke forlate klasserommet");
        }
        var ct = classroomTeacherRepository.findOneByClassroomIdAndUserId(classroomId, targetUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        classroomTeacherRepository.delete(ct);
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

}
