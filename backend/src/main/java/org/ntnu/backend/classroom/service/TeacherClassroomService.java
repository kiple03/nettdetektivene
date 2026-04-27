package org.ntnu.backend.classroom.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CoTeacherResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.InviteCoTeacherRequest;
import org.ntnu.backend.classroom.dto.MysterySubmissionResponse;
import org.ntnu.backend.classroom.dto.ReviewMysteryRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateStudentUsernameRequest;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.classroom.mapper.ClassroomMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.dto.progress.ProgressSummaryResponse;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.service.ProgressService;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.mystery.repository.MysterySubmissionRepository;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.service.NoteService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.ntnu.backend.user.repository.UserRepository;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.AllArgsConstructor;

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
    private final ProgressService progressService;
    private final ClassroomMapper classroomMapper;
    private final UserInventoryItemRepository userInventoryItemRepository;
    private final LeaderboardService leaderboardService;
    private final MedalionService medalionService;
    private final ProfanityFilterService profanityFilter;
    private final NoteService noteService;

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
            .flatMap(c -> enrollmentRepository.findByClassroomIdAndLeftAtIsNull(c.getId()).stream())
            .map(e -> e.getUser().getId())
            .toList();


        Map<UUID, ProgressSummaryResponse> progressByStudent =
            progressService.getProgressForStudents(allStudentIds);

        Map<String, Integer> avgScoreByClassroom = leaderboardService.getTeacherClassroomScores(teacherId)
            .stream()
            .collect(Collectors.toMap(LeaderboardClassroomResponse::id, LeaderboardClassroomResponse::avgScore));

        return classrooms.stream()
            .map(classroom -> summarizeClassroom(classroom, progressByStudent, totalLevelsInGame,
                avgScoreByClassroom.getOrDefault(classroom.getId().toString(), 0)))
            .toList();
    }

    private ClassroomSummaryResponse summarizeClassroom(
        Classroom classroom,
        Map<UUID, ProgressSummaryResponse> progressByStudent,
        int totalLevelsInGame,
        int avgScore) {

        List<Enrollment> activeEnrollments =
            enrollmentRepository.findByClassroomIdAndLeftAtIsNull(classroom.getId());

        int completedCount = activeEnrollments.stream()
            .map(e -> progressByStudent.get(e.getUser().getId()))
            .filter(Objects::nonNull)
            .mapToInt(ProgressSummaryResponse::totalLevelsCompleted)
            .sum();

        return classroomMapper.toSummary(
            classroom, activeEnrollments.size(), totalLevelsInGame, completedCount, avgScore);
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

        List<Enrollment> enrollments =
            enrollmentRepository.findByClassroomIdAndLeftAtIsNull(classroomId);
        List<UUID> studentIds = enrollments.stream()
            .map(e -> e.getUser().getId())
            .toList();

        Map<UUID, ProgressSummaryResponse> progressByStudent =
            progressService.getProgressForStudents(studentIds);

        Map<UUID, List<EquippedItemDto>> equippedItemsByStudent =
            userInventoryItemRepository.findByUserIdInAndIsEquippedTrue(studentIds).stream()
                .collect(Collectors.groupingBy(
                    item -> item.getUser().getId(),
                    Collectors.mapping(item -> EquippedItemDto.builder()
                            .id(item.getAvatarItem().getId())
                            .name(item.getAvatarItem().getName())
                            .category(item.getAvatarItem().getCategory())
                            .imageUrl("/assets/" + (item.getAvatarItem().getImageFile() != null ? item.getAvatarItem().getImageFile() : item.getAvatarItem().getName() + ".svg"))
                            .build(),
                        Collectors.toList())
                ));

        Map<UUID, Integer> scores = leaderboardService.computeScores(enrollments);
        Map<UUID, Integer> medalsByStudent = medalionService.getMedalCountsForUsers(studentIds);

        return enrollments.stream()
            .map(e -> classroomMapper.toStudentProgress(
                e,
                progressByStudent.get(e.getUser().getId()),
                scores.getOrDefault(e.getUser().getId(), 0),
                medalsByStudent.getOrDefault(e.getUser().getId(), 0),
                equippedItemsByStudent.getOrDefault(e.getUser().getId(), List.of())
            ))
            .sorted(Comparator
                .comparingInt(StudentProgressResponse::getScore)
                .reversed()
                .thenComparing(StudentProgressResponse::getUsername, String.CASE_INSENSITIVE_ORDER))
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

    @Transactional(readOnly = true)
    public List<MedalionResponse> getStudentMedalions(UUID classroomId, UUID studentId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return medalionService.getAllUsersMedalion(studentId);
    }

    @Transactional(readOnly = true)
    public NotesOverviewResponse getStudentNotebook(UUID classroomId, UUID studentId, JwtAuthenticatedPrincipal principal) {
        classroomAuthorizationService.verifyAccess(principal, classroomId);
        enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return noteService.getNotesForUser(studentId, classroomId);
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
        if (profanityFilter.containsProfanity(newUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brukernavnet inneholder upassende innhold");
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

        Enrollment enrollment = enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ProgressSummaryResponse progress =
            progressService.getProgressForStudents(List.of(studentId)).get(studentId);

        return classroomMapper.toStudentDetail(enrollment, progress);
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

        List<ClassroomTeacher> allTeachers = classroomTeacherRepository.findWithUsersByClassroomId(classroomId);
        if (allTeachers.size() == 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Du er eneste lærer i klasserommet. Slett klasserommet i stedet.");
        }

        boolean targetIsOwner = classroomTeacherRepository.isOwnerOfClassroom(classroomId, targetUserId);
        if (targetIsOwner) {
            allTeachers.stream()
                .filter(ct -> !ct.getId().getUserId().equals(targetUserId))
                .findFirst()
                .ifPresent(ct -> {
                    ct.setOwner(true);
                    classroomTeacherRepository.save(ct);
                });
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
        return classroomMapper.toResponse(classroom);
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
