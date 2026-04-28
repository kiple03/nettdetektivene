package org.ntnu.backend.classroom.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.ntnu.backend.classroom.dto.ClassroomResponse;
import org.ntnu.backend.classroom.dto.CreateClassroomRequest;
import org.ntnu.backend.classroom.dto.JoinClassroomRequest;
import org.ntnu.backend.classroom.dto.UpdateClassroomRequest;
import org.ntnu.backend.classroom.mapper.ClassroomMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.classroom.util.JoinCodeGenerator;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.mystery.repository.MysterySubmissionRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ClassroomServiceTest {

    private ClassroomRepository classroomRepository;
    private UserRepository userRepository;
    private EnrollmentRepository enrollmentRepository;
    private ClassroomTeacherRepository classroomTeacherRepository;
    private MysterySubmissionRepository mysterySubmissionRepository;
    private ClassroomCensorWordRepository classroomCensorWordRepository;
    private ProfanityFilterService profanityFilterService;
    private ClassroomMapper classroomMapper;
    private JoinCodeGenerator joinCodeGenerator;
    private MedalionService medalionService;

    private ClassroomService classroomService;

    private UUID classroomId;
    private UUID userId;
    private UUID teacherId;

    private Classroom classroom;
    private User student;
    private User teacher;
    private ClassroomResponse classroomResponse;

    @BeforeEach
    void setUp() {
        classroomRepository = mock(ClassroomRepository.class);
        userRepository = mock(UserRepository.class);
        enrollmentRepository = mock(EnrollmentRepository.class);
        classroomTeacherRepository = mock(ClassroomTeacherRepository.class);
        mysterySubmissionRepository = mock(MysterySubmissionRepository.class);
        classroomCensorWordRepository = mock(ClassroomCensorWordRepository.class);
        profanityFilterService = mock(ProfanityFilterService.class);
        classroomMapper = mock(ClassroomMapper.class);
        joinCodeGenerator = mock(JoinCodeGenerator.class);
        medalionService = mock(MedalionService.class);

        classroomService = new ClassroomService(
                classroomRepository,
                userRepository,
                enrollmentRepository,
                classroomTeacherRepository,
                mysterySubmissionRepository,
                classroomCensorWordRepository,
                profanityFilterService,
                classroomMapper,
                joinCodeGenerator,
                medalionService
        );

        classroomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        teacherId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        classroom = Classroom.builder()
                .id(classroomId)
                .title("Klasse 5A")
                .description("Test classroom")
                .joinCode("ABC123")
                .createdAt(LocalDateTime.of(2026, 4, 28, 12, 30))
                .build();

        student = User.builder()
                .id(userId)
                .username("studentuser")
                .email("student@example.com")
                .build();

        teacher = User.builder()
                .id(teacherId)
                .username("teacheruser")
                .email("teacher@example.com")
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
    void createClassroom_shouldCreateClassroomAndAssignTeacherAsOwner() {
        CreateClassroomRequest request = new CreateClassroomRequest();
        request.setTitle("Klasse 5A");
        request.setDescription("Test classroom");

        Classroom unsavedClassroom = Classroom.builder()
                .title("Klasse 5A")
                .description("Test classroom")
                .joinCode("ABC123")
                .build();

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(joinCodeGenerator.generateUniqueCode()).thenReturn("ABC123");
        when(classroomMapper.toEntity(request, "ABC123")).thenReturn(unsavedClassroom);
        when(classroomRepository.save(unsavedClassroom)).thenReturn(classroom);
        when(classroomMapper.toResponse(classroom)).thenReturn(classroomResponse);

        ClassroomResponse result = classroomService.createClassroom(request, teacherId);

        assertEquals(classroomResponse, result);

        verify(userRepository).findById(teacherId);
        verify(joinCodeGenerator).generateUniqueCode();
        verify(classroomMapper).toEntity(request, "ABC123");
        verify(classroomRepository).save(unsavedClassroom);
        verify(classroomMapper).toResponse(classroom);

        ArgumentCaptor<ClassroomTeacher> captor = ArgumentCaptor.forClass(ClassroomTeacher.class);
        verify(classroomTeacherRepository).save(captor.capture());

        ClassroomTeacher savedTeacherLink = captor.getValue();
        assertEquals(classroom, savedTeacherLink.getClassroom());
        assertEquals(teacher, savedTeacherLink.getUser());
        assertTrue(savedTeacherLink.isOwner());
        assertEquals(classroomId, savedTeacherLink.getId().getClassroomId());
        assertEquals(teacherId, savedTeacherLink.getId().getUserId());
    }

    @Test
    void createClassroom_whenTeacherNotFound_shouldThrowNotFound() {
        CreateClassroomRequest request = new CreateClassroomRequest();
        request.setTitle("Klasse 5A");

        when(userRepository.findById(teacherId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.createClassroom(request, teacherId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(userRepository).findById(teacherId);
        verifyNoInteractions(joinCodeGenerator);
        verifyNoInteractions(classroomMapper);
        verify(classroomRepository, never()).save(any());
        verify(classroomTeacherRepository, never()).save(any());
    }

    @Test
    void updateClassroom_shouldUpdateAndReturnClassroom() {
        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle("Updated title");
        request.setDescription("Updated description");

        Classroom savedClassroom = Classroom.builder()
                .id(classroomId)
                .title("Updated title")
                .description("Updated description")
                .joinCode("ABC123")
                .build();

        ClassroomResponse updatedResponse = ClassroomResponse.builder()
                .id(classroomId.toString())
                .title("Updated title")
                .description("Updated description")
                .joinCode("ABC123")
                .build();

        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        when(classroomRepository.save(classroom)).thenReturn(savedClassroom);
        when(classroomMapper.toResponse(savedClassroom)).thenReturn(updatedResponse);

        ClassroomResponse result = classroomService.updateClassroom(classroomId, request);

        assertEquals(updatedResponse, result);

        verify(classroomRepository).findById(classroomId);
        verify(classroomMapper).applyUpdates(classroom, request);
        verify(classroomRepository).save(classroom);
        verify(classroomMapper).toResponse(savedClassroom);
    }

    @Test
    void updateClassroom_whenClassroomNotFound_shouldThrowNotFound() {
        UpdateClassroomRequest request = new UpdateClassroomRequest();
        request.setTitle("Updated title");

        when(classroomRepository.findById(classroomId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.updateClassroom(classroomId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomRepository).findById(classroomId);
        verify(classroomMapper, never()).applyUpdates(any(), any());
        verify(classroomRepository, never()).save(any());
    }

    @Test
    void deleteClassroom_shouldDeleteClassroomAndRelatedData() {
        when(classroomRepository.existsById(classroomId)).thenReturn(true);

        classroomService.deleteClassroom(classroomId);

        verify(classroomRepository).existsById(classroomId);
        verify(mysterySubmissionRepository).deleteByEnrollmentClassroomId(classroomId);
        verify(enrollmentRepository).deleteByClassroomId(classroomId);
        verify(classroomTeacherRepository).deleteByClassroomId(classroomId);
        verify(classroomCensorWordRepository).deleteByClassroomId(classroomId);
        verify(profanityFilterService).evictClassroomCache(classroomId);
        verify(classroomRepository).deleteById(classroomId);
    }

    @Test
    void deleteClassroom_whenClassroomDoesNotExist_shouldThrowNotFound() {
        when(classroomRepository.existsById(classroomId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.deleteClassroom(classroomId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomRepository).existsById(classroomId);
        verify(mysterySubmissionRepository, never()).deleteByEnrollmentClassroomId(any());
        verify(enrollmentRepository, never()).deleteByClassroomId(any());
        verify(classroomTeacherRepository, never()).deleteByClassroomId(any());
        verify(classroomCensorWordRepository, never()).deleteByClassroomId(any());
        verify(profanityFilterService, never()).evictClassroomCache(any());
        verify(classroomRepository, never()).deleteById(any());
    }

    @Test
    void joinClassroom_shouldEnrollStudentAwardMedalionAndReturnClassroom() {
        JoinClassroomRequest request = new JoinClassroomRequest("ABC123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(classroomRepository.findByJoinCode("ABC123")).thenReturn(Optional.of(classroom));
        when(enrollmentRepository.existsByUserAndLeftAtIsNull(student)).thenReturn(false);
        when(classroomMapper.toResponse(classroom)).thenReturn(classroomResponse);

        ClassroomResponse result = classroomService.joinClassroom(request, userId);

        assertEquals(classroomResponse, result);

        verify(userRepository).findById(userId);
        verify(classroomRepository).findByJoinCode("ABC123");
        verify(enrollmentRepository).existsByUserAndLeftAtIsNull(student);

        ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());

        Enrollment savedEnrollment = enrollmentCaptor.getValue();
        assertEquals(student, savedEnrollment.getUser());
        assertEquals(classroom, savedEnrollment.getClassroom());

        verify(medalionService).awardInternalUsage(
                userId,
                "Hysj-hysj",
                "Hemmelig innpass, du kom deg inn i klasserommet"
        );

        verify(classroomMapper).toResponse(classroom);
    }

    @Test
    void joinClassroom_whenUserNotFound_shouldThrowNotFound() {
        JoinClassroomRequest request = new JoinClassroomRequest("ABC123");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.joinClassroom(request, userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(userRepository).findById(userId);
        verify(classroomRepository, never()).findByJoinCode(anyString());
        verify(enrollmentRepository, never()).save(any());
        verifyNoInteractions(medalionService);
    }

    @Test
    void joinClassroom_whenUsernameIsMissing_shouldThrowConflict() {
        student.setUsername(null);

        JoinClassroomRequest request = new JoinClassroomRequest("ABC123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.joinClassroom(request, userId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository).findById(userId);
        verify(classroomRepository, never()).findByJoinCode(anyString());
        verify(enrollmentRepository, never()).save(any());
        verifyNoInteractions(medalionService);
    }

    @Test
    void joinClassroom_whenUsernameIsBlank_shouldThrowConflict() {
        student.setUsername("   ");

        JoinClassroomRequest request = new JoinClassroomRequest("ABC123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.joinClassroom(request, userId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository).findById(userId);
        verify(classroomRepository, never()).findByJoinCode(anyString());
        verify(enrollmentRepository, never()).save(any());
        verifyNoInteractions(medalionService);
    }

    @Test
    void joinClassroom_whenJoinCodeIsInvalid_shouldThrowNotFound() {
        JoinClassroomRequest request = new JoinClassroomRequest("ABC123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(classroomRepository.findByJoinCode("ABC123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.joinClassroom(request, userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(userRepository).findById(userId);
        verify(classroomRepository).findByJoinCode("ABC123");
        verify(enrollmentRepository, never()).save(any());
        verifyNoInteractions(medalionService);
    }

    @Test
    void joinClassroom_whenStudentAlreadyInClassroom_shouldThrowConflict() {
        JoinClassroomRequest request = new JoinClassroomRequest("ABC123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(classroomRepository.findByJoinCode("ABC123")).thenReturn(Optional.of(classroom));
        when(enrollmentRepository.existsByUserAndLeftAtIsNull(student)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.joinClassroom(request, userId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository).findById(userId);
        verify(classroomRepository).findByJoinCode("ABC123");
        verify(enrollmentRepository).existsByUserAndLeftAtIsNull(student);
        verify(enrollmentRepository, never()).save(any());
        verifyNoInteractions(medalionService);
    }

    @Test
    void getClassroom_shouldReturnClassroom() {
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.of(classroom));
        when(classroomMapper.toResponse(classroom)).thenReturn(classroomResponse);

        ClassroomResponse result = classroomService.getClassroom(classroomId);

        assertEquals(classroomResponse, result);

        verify(classroomRepository).findById(classroomId);
        verify(classroomMapper).toResponse(classroom);
    }

    @Test
    void getClassroom_whenClassroomNotFound_shouldThrowNotFound() {
        when(classroomRepository.findById(classroomId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classroomService.getClassroom(classroomId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classroomRepository).findById(classroomId);
        verify(classroomMapper, never()).toResponse(any());
    }

    @Test
    void getMyClassroom_whenEnrollmentExists_shouldReturnClassroomResponse() {
        Enrollment enrollment = Enrollment.builder()
                .id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                .user(student)
                .classroom(classroom)
                .build();

        when(enrollmentRepository.findByUserIdAndLeftAtIsNull(userId))
                .thenReturn(Optional.of(enrollment));
        when(classroomMapper.toResponse(classroom)).thenReturn(classroomResponse);

        Optional<ClassroomResponse> result = classroomService.getMyClassroom(userId);

        assertTrue(result.isPresent());
        assertEquals(classroomResponse, result.get());

        verify(enrollmentRepository).findByUserIdAndLeftAtIsNull(userId);
        verify(classroomMapper).toResponse(classroom);
    }

    @Test
    void getMyClassroom_whenEnrollmentDoesNotExist_shouldReturnEmptyOptional() {
        when(enrollmentRepository.findByUserIdAndLeftAtIsNull(userId))
                .thenReturn(Optional.empty());

        Optional<ClassroomResponse> result = classroomService.getMyClassroom(userId);

        assertTrue(result.isEmpty());

        verify(enrollmentRepository).findByUserIdAndLeftAtIsNull(userId);
        verify(classroomMapper, never()).toResponse(any());
    }
}