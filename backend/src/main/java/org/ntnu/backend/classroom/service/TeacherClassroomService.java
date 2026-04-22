package org.ntnu.backend.classroom.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.StopResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.MysterySubmissionResponse;
import org.ntnu.backend.classroom.dto.ReviewMysteryRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.StudentProgressResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.mapper.ClassroomMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.Enrollment;
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
    private final ClassroomMapper classroomMapper;

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

        return classrooms.stream()
            .map(classroom -> summarizeClassroom(classroom, progressByStudent, totalLevelsInGame))
            .toList();
    }

    private ClassroomSummaryResponse summarizeClassroom(
        Classroom classroom,
        Map<UUID, ProgressSummaryResponse> progressByStudent,
        int totalLevelsInGame) {

        List<Enrollment> activeEnrollments =
            enrollmentRepository.findByClassroomIdAndLeftAtIsNull(classroom.getId());

        int completedCount = activeEnrollments.stream()
            .map(e -> progressByStudent.get(e.getUser().getId()))
            .filter(Objects::nonNull)
            .mapToInt(ProgressSummaryResponse::totalLevelsCompleted)
            .sum();

        return classroomMapper.toSummary(
            classroom, activeEnrollments.size(), totalLevelsInGame, completedCount);
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

        return enrollments.stream()
            .map(e -> classroomMapper.toStudentProgress(
                e, progressByStudent.get(e.getUser().getId())))
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

        Enrollment enrollment = enrollmentRepository
            .findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ProgressSummaryResponse progress =
            progressService.getProgressForStudents(List.of(studentId)).get(studentId);

        return classroomMapper.toStudentDetail(enrollment, progress);
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
