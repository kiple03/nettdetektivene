package org.ntnu.backend.classroom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.ClassroomSummaryResponse;
import org.ntnu.backend.classroom.dto.CoTeacherResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.StudentDetailResponse;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateStudentUsernameRequest;
import org.ntnu.backend.classroom.dto.leaderboard.LeaderboardClassroomResponse;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.classroom.mapper.ClassroomMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.ClassroomTeacherId;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.service.ProgressService;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.mystery.repository.MysterySubmissionRepository;
import org.ntnu.backend.notes.dto.NotesOverviewResponse;
import org.ntnu.backend.notes.service.NoteService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.ntnu.backend.user.repository.UserRepository;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TeacherClassroomServiceTest {

    private ClassroomTeacherRepository classroomTeacherRepository;
    private ClassroomRepository classroomRepository;
    private ClassroomService classroomService;
    private ClassroomAuthorizationService classroomAuthorizationService;
    private EnrollmentRepository enrollmentRepository;
    private LevelRepository levelRepository;
    private StopRepository stopRepository;
    private MysterySubmissionRepository mysterySubmissionRepository;
    private UserRepository userRepository;
    private ProgressService progressService;
    private ClassroomMapper classroomMapper;
    private UserInventoryItemRepository userInventoryItemRepository;
    private LeaderboardService leaderboardService;
    private MedalionService medalionService;
    private ProfanityFilterService profanityFilter;
    private NoteService noteService;

    private TeacherClassroomService teacherClassroomService;

    private JwtAuthenticatedPrincipal principal;

    private UUID classroomId;
    private UUID teacherId;
    private UUID studentId;
    private UUID coTeacherId;

    private Classroom classroom;
    private User teacher;
    private User student;
    private User coTeacher;
    private Enrollment enrollment;
    private ClassroomResponse classroomResponse;

    @BeforeEach
    void setUp() {
        classroomTeacherRepository = mock(ClassroomTeacherRepository.class);
        classroomRepository = mock(ClassroomRepository.class);
        classroomService = mock(ClassroomService.class);
        classroomAuthorizationService = mock(ClassroomAuthorizationService.class);
        enrollmentRepository = mock(EnrollmentRepository.class);
        levelRepository = mock(LevelRepository.class);
        stopRepository = mock(StopRepository.class);
        mysterySubmissionRepository = mock(MysterySubmissionRepository.class);
        userRepository = mock(UserRepository.class);
        progressService = mock(ProgressService.class);
        classroomMapper = mock(ClassroomMapper.class);
        userInventoryItemRepository = mock(UserInventoryItemRepository.class);
        leaderboardService = mock(LeaderboardService.class);
        medalionService = mock(MedalionService.class);
        profanityFilter = mock(ProfanityFilterService.class);
        noteService = mock(NoteService.class);

        teacherClassroomService = new TeacherClassroomService(
                classroomTeacherRepository,
                classroomRepository,
                classroomService,
                classroomAuthorizationService,
                enrollmentRepository,
                levelRepository,
                stopRepository,
                mysterySubmissionRepository,
                userRepository,
                progressService,
                classroomMapper,
                userInventoryItemRepository,
                leaderboardService,
                medalionService,
                profanityFilter,
                noteService
        );

        principal = mock(JwtAuthenticatedPrincipal.class);

        classroomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        teacherId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        studentId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        coTeacherId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        when(principal.getUserId()).thenReturn(teacherId);

        classroom = Classroom.builder()
                .id(classroomId)
                .title("Klasse 5A")
                .description("Test classroom")
                .joinCode("ABC123")
                .createdAt(LocalDateTime.of(2026, 4, 28, 12, 30))
                .build();

        teacher = User.builder()
                .id(teacherId)
                .username("Teacher")
                .email("teacher@example.com")
                .role(RoleEnum.TEACHER)
                .build();

        student = User.builder()
                .id(studentId)
                .username("Student")
                .email("student@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        coTeacher = User.builder()
                .id(coTeacherId)
                .username("CoTeacher")
                .email("coteacher@example.com")
                .role(RoleEnum.TEACHER)
                .build();

        enrollment = Enrollment.builder()
                .id(UUID.fromString("55555555-5555-5555-5555-555555555555"))
                .user(student)
                .classroom(classroom)
                .build();

        classroomResponse = ClassroomResponse.builder()
                .id(classroomId.toString())
                .title("Klasse 5A")
                .description("Test classroom")
                .joinCode("ABC123")
                .createdAt(LocalDateTime.of(2026, 4, 28, 12, 30))
                .build();
    }

    @Test
    void createClassroom_shouldDelegateToClassroomService() {
        CreateClassroomRequest request = new CreateClassroomRequest();
        request.setTitle("Klasse 5A");
        request.setDescription("Test classroom");

        when(classroomService.createClassroom(request, teacherId)).thenReturn(classroomResponse);

        ClassroomResponse result = teacherClassroomService.createClassroom(request, teacherId);

        assertEquals(classroomResponse, result);
        verify(classroomService).createClassroom(request, teacherId);
        verifyNoInteractions(classroomAuthorizationService);
    }

    @Test
    void listClassrooms_shouldReturnClassroomSummaries() {
        ClassroomSummaryResponse summary = mock(ClassroomSummaryResponse.class);

        when(classroomTeacherRepository.findClassroomsByTeacherId(teacherId))
                .thenReturn(List.of(classroom));
        when(stopRepository.count()).thenReturn(5L);
        when(enrollmentRepository.findByClassroomIdAndLeftAtIsNull(classroomId))
                .thenReturn(List.of(enrollment));
        when(progressService.getProgressForStudents(List.of(studentId)))
                .thenReturn(Map.of());
        when(leaderboardService.getTeacherClassroomScores(teacherId))
                .thenReturn(List.of(new LeaderboardClassroomResponse(
                        classroomId.toString(),
                        "Klasse 5A",
                        800,
                        400
                )));
        when(classroomMapper.toSummary(classroom, 1, 5, 0.0, 400))
                .thenReturn(summary);

        List<ClassroomSummaryResponse> result = teacherClassroomService.listClassrooms(teacherId);

        assertEquals(List.of(summary), result);

        verify(classroomTeacherRepository).findClassroomsByTeacherId(teacherId);
        verify(stopRepository).count();
        verify(enrollmentRepository, times(2)).findByClassroomIdAndLeftAtIsNull(classroomId);
        verify(progressService).getProgressForStudents(List.of(studentId));
        verify(leaderboardService).getTeacherClassroomScores(teacherId);
        verify(classroomMapper).toSummary(classroom, 1, 5, 0.0, 400);
    }

    @Test
    void updateClassroom_shouldVerifyAccessAndDelegateToClassroomService() {
        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle("Updated title");

        when(classroomService.updateClassroom(classroomId, request)).thenReturn(classroomResponse);

        ClassroomResponse result =
                teacherClassroomService.updateClassroom(classroomId, request, principal);

        assertEquals(classroomResponse, result);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomService).updateClassroom(classroomId, request);
    }

    @Test
    void updateClassroom_whenAccessDenied_shouldNotUpdateClassroom() {
        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle("Updated title");

        doThrow(new AccessDeniedException("No access"))
                .when(classroomAuthorizationService)
                .verifyAccess(principal, classroomId);

        assertThrows(
                AccessDeniedException.class,
                () -> teacherClassroomService.updateClassroom(classroomId, request, principal)
        );

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomService, never()).updateClassroom(any(), any());
    }

    @Test
    void deleteClassroom_shouldVerifyAccessAndDelegateToClassroomService() {
        teacherClassroomService.deleteClassroom(classroomId, principal);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomService).deleteClassroom(classroomId);
    }

    @Test
    void deleteClassroom_whenAccessDenied_shouldNotDelete() {
        doThrow(new AccessDeniedException("No access"))
                .when(classroomAuthorizationService)
                .verifyAccess(principal, classroomId);

        assertThrows(
                AccessDeniedException.class,
                () -> teacherClassroomService.deleteClassroom(classroomId, principal)
        );

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomService, never()).deleteClassroom(any());
    }

    @Test
    void removeStudent_shouldSetLeftAtAndSaveEnrollment() {
        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.of(enrollment));

        teacherClassroomService.removeStudent(classroomId, studentId, principal);

        assertNotNull(enrollment.getLeftAt());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void removeStudent_whenEnrollmentNotFound_shouldThrowNotFound() {
        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> teacherClassroomService.removeStudent(classroomId, studentId, principal)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void getStudentMedalions_shouldVerifyEnrollmentAndReturnMedalions() {
        MedalionResponse medalion = mock(MedalionResponse.class);

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.of(enrollment));
        when(medalionService.getAllUsersMedalion(studentId))
                .thenReturn(List.of(medalion));

        List<MedalionResponse> result =
                teacherClassroomService.getStudentMedalions(classroomId, studentId, principal);

        assertEquals(List.of(medalion), result);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verify(medalionService).getAllUsersMedalion(studentId);
    }

    @Test
    void getStudentMedalions_whenEnrollmentNotFound_shouldThrowNotFound() {
        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> teacherClassroomService.getStudentMedalions(classroomId, studentId, principal)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verifyNoInteractions(medalionService);
    }

    @Test
    void getStudentNotebook_shouldVerifyEnrollmentAndReturnNotebook() {
        NotesOverviewResponse notebook = mock(NotesOverviewResponse.class);

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.of(enrollment));
        when(noteService.getNotesForUser(studentId, classroomId))
                .thenReturn(notebook);

        NotesOverviewResponse result =
                teacherClassroomService.getStudentNotebook(classroomId, studentId, principal);

        assertEquals(notebook, result);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verify(noteService).getNotesForUser(studentId, classroomId);
    }

    @Test
    void updateStudentUsername_shouldTrimUsernameAndSaveUser() {
        UpdateStudentUsernameRequest request = new UpdateStudentUsernameRequest("  NewName  ");

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.of(enrollment));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(profanityFilter.containsProfanity("NewName")).thenReturn(false);

        teacherClassroomService.updateStudentUsername(classroomId, studentId, request, principal);

        assertEquals("NewName", student.getUsername());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verify(userRepository).findById(studentId);
        verify(profanityFilter).containsProfanity("NewName");
        verify(userRepository).save(student);
    }

    @Test
    void updateStudentUsername_whenUsernameContainsProfanity_shouldThrowBadRequestAndNotSave() {
        UpdateStudentUsernameRequest request = new UpdateStudentUsernameRequest(" badname ");

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.of(enrollment));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(profanityFilter.containsProfanity("badname")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> teacherClassroomService.updateStudentUsername(classroomId, studentId, request, principal)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateStudentUsername_whenUserNotFound_shouldThrowNotFound() {
        UpdateStudentUsernameRequest request = new UpdateStudentUsernameRequest("NewName");

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.of(enrollment));
        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> teacherClassroomService.updateStudentUsername(classroomId, studentId, request, principal)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(userRepository).findById(studentId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getStudentDetail_shouldReturnStudentDetail() {
        StudentDetailResponse detail = mock(StudentDetailResponse.class);

        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.of(enrollment));
        when(progressService.getProgressForStudents(List.of(studentId)))
                .thenReturn(Map.of());
        when(classroomMapper.toStudentDetail(enrollment, null))
                .thenReturn(detail);

        StudentDetailResponse result =
                teacherClassroomService.getStudentDetail(classroomId, studentId, principal);

        assertEquals(detail, result);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verify(progressService).getProgressForStudents(List.of(studentId));
        verify(classroomMapper).toStudentDetail(enrollment, null);
    }

    @Test
    void getStudentDetail_whenEnrollmentNotFound_shouldThrowNotFound() {
        when(enrollmentRepository.findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> teacherClassroomService.getStudentDetail(classroomId, studentId, principal)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(enrollmentRepository).findByClassroomIdAndUserIdAndLeftAtIsNull(classroomId, studentId);
        verifyNoInteractions(progressService);
        verify(classroomMapper, never()).toStudentDetail(any(), any());
    }

    @Test
    void listCoTeachers_shouldReturnCoTeachers() {
        ClassroomTeacher ownerEntry = ClassroomTeacher.builder()
                .id(new ClassroomTeacherId(classroomId, teacherId))
                .classroom(classroom)
                .user(teacher)
                .isOwner(true)
                .build();

        ClassroomTeacher coTeacherEntry = ClassroomTeacher.builder()
                .id(new ClassroomTeacherId(classroomId, coTeacherId))
                .classroom(classroom)
                .user(coTeacher)
                .isOwner(false)
                .build();

        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(ownerEntry, coTeacherEntry));

        List<CoTeacherResponse> result =
                teacherClassroomService.listCoTeachers(classroomId, principal);

        assertEquals(2, result.size());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomTeacherRepository).findWithUsersByClassroomId(classroomId);
    }

    @Test
    void removeCoTeacher_whenRequesterIsOwner_shouldDeleteTarget() {
        ClassroomTeacher ownerEntry = ClassroomTeacher.builder()
                .id(new ClassroomTeacherId(classroomId, teacherId))
                .classroom(classroom)
                .user(teacher)
                .isOwner(true)
                .build();

        ClassroomTeacher targetEntry = ClassroomTeacher.builder()
                .id(new ClassroomTeacherId(classroomId, coTeacherId))
                .classroom(classroom)
                .user(coTeacher)
                .isOwner(false)
                .build();

        when(classroomTeacherRepository.isOwnerOfClassroom(classroomId, teacherId))
                .thenReturn(true);
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(ownerEntry, targetEntry));
        when(classroomTeacherRepository.isOwnerOfClassroom(classroomId, coTeacherId))
                .thenReturn(false);
        when(classroomTeacherRepository.findOneByClassroomIdAndUserId(classroomId, coTeacherId))
                .thenReturn(Optional.of(targetEntry));

        teacherClassroomService.removeCoTeacher(classroomId, coTeacherId, principal);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomTeacherRepository).isOwnerOfClassroom(classroomId, teacherId);
        verify(classroomTeacherRepository).findWithUsersByClassroomId(classroomId);
        verify(classroomTeacherRepository).delete(targetEntry);
    }

    @Test
    void removeCoTeacher_whenRequesterIsNotOwnerAndNotRemovingSelf_shouldThrowAccessDenied() {
        when(classroomTeacherRepository.isOwnerOfClassroom(classroomId, teacherId))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> teacherClassroomService.removeCoTeacher(classroomId, coTeacherId, principal)
        );

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomTeacherRepository).isOwnerOfClassroom(classroomId, teacherId);
        verify(classroomTeacherRepository, never()).delete(any());
    }

    @Test
    void removeCoTeacher_whenOnlyTeacherLeft_shouldThrowConflict() {
        ClassroomTeacher onlyTeacher = ClassroomTeacher.builder()
                .id(new ClassroomTeacherId(classroomId, teacherId))
                .classroom(classroom)
                .user(teacher)
                .isOwner(true)
                .build();

        when(classroomTeacherRepository.isOwnerOfClassroom(classroomId, teacherId))
                .thenReturn(true);
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(onlyTeacher));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> teacherClassroomService.removeCoTeacher(classroomId, teacherId, principal)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomTeacherRepository, never()).delete(any());
    }

    @Test
    void getClassroom_shouldVerifyAccessAndReturnClassroom() {
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        when(classroomMapper.toResponse(classroom)).thenReturn(classroomResponse);

        ClassroomResponse result = teacherClassroomService.getClassroom(classroomId, principal);

        assertEquals(classroomResponse, result);

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomRepository).findById(classroomId);
        verify(classroomMapper).toResponse(classroom);
    }

    @Test
    void getClassroom_whenClassroomNotFound_shouldThrowNotFound() {
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> teacherClassroomService.getClassroom(classroomId, principal)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
        verify(classroomRepository).findById(classroomId);
        verify(classroomMapper, never()).toResponse(any());
    }
}