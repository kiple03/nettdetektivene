package org.ntnu.backend.classroom.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.mapper.NotificationMapper;
import org.ntnu.backend.classroom.model.Classroom;
import org.ntnu.backend.classroom.model.ClassroomTeacher;
import org.ntnu.backend.classroom.model.Enrollment;
import org.ntnu.backend.classroom.model.Notification;
import org.ntnu.backend.classroom.model.enums.NotificationType;
import org.ntnu.backend.classroom.repository.ClassroomTeacherRepository;
import org.ntnu.backend.classroom.repository.EnrollmentRepository;
import org.ntnu.backend.classroom.repository.NotificationRepository;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.UserProgress;
import org.ntnu.backend.game.repository.UserProgressRepository;
import org.ntnu.backend.user.model.User;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ClassroomTeacherRepository classroomTeacherRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    private final NotificationMapper notificationMapper = new NotificationMapper();

    private NotificationService notificationService;

    private final UUID teacherId = UUID.randomUUID();
    private final UUID classroomId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();
    private final UUID notificationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                enrollmentRepository,
                classroomTeacherRepository,
                userProgressRepository,
                notificationMapper
        );
    }

    // ===== createNotification =====

    @Test
    void createNotification_savesAndReturnsResponse() {
        Notification saved = buildNotification(notificationId, false);
        when(notificationRepository.save(any())).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(
                teacherId,
                classroomId,
                studentId,
                NotificationType.AREA_COMPLETED,
                "test message"
        );

        assertThat(response.getId()).isEqualTo(notificationId);
        assertThat(response.isRead()).isFalse();
        assertThat(response.getType()).isEqualTo(NotificationType.AREA_COMPLETED);
        assertThat(response.getTeacherId()).isEqualTo(teacherId);
        assertThat(response.getClassroomId()).isEqualTo(classroomId);
        assertThat(response.getStudentId()).isEqualTo(studentId);

        verify(notificationRepository).save(any(Notification.class));
    }

    // ===== getNotificationsForTeacher =====

    @Test
    void getNotificationsForTeacher_returnsAllForTeacher() {
        Notification n1 = buildNotification(UUID.randomUUID(), false);
        Notification n2 = buildNotification(UUID.randomUUID(), true);

        when(notificationRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId))
                .thenReturn(List.of(n1, n2));

        List<NotificationResponse> result =
                notificationService.getNotificationsForTeacher(teacherId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(n1.getId());
        assertThat(result.get(1).getId()).isEqualTo(n2.getId());

        verify(notificationRepository).findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Test
    void getNotificationsForTeacher_filteredByClassroom() {
        Notification notification = buildNotification(UUID.randomUUID(), false);

        when(notificationRepository.findByTeacherIdAndClassroomIdOrderByCreatedAtDesc(
                teacherId,
                classroomId
        )).thenReturn(List.of(notification));

        List<NotificationResponse> result =
                notificationService.getNotificationsForTeacher(teacherId, classroomId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClassroomId()).isEqualTo(classroomId);

        verify(notificationRepository)
                .findByTeacherIdAndClassroomIdOrderByCreatedAtDesc(teacherId, classroomId);
    }

    // ===== markAsRead =====

    @Test
    void markAsRead_setsReadAndReturnsResponse() {
        Notification notification = buildNotification(notificationId, false);

        when(notificationRepository.findByIdAndTeacherId(notificationId, teacherId))
                .thenReturn(Optional.of(notification));

        NotificationResponse response =
                notificationService.markAsRead(notificationId, teacherId);

        assertThat(notification.isRead()).isTrue();
        assertThat(response.isRead()).isTrue();

        verify(notificationRepository).findByIdAndTeacherId(notificationId, teacherId);
    }

    @Test
    void markAsRead_throwsWhenNotFound() {
        when(notificationRepository.findByIdAndTeacherId(notificationId, teacherId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(notificationId, teacherId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    // ===== deleteNotification =====

    @Test
    void deleteNotification_deletesWhenOwner() {
        Notification notification = buildNotification(notificationId, false);

        when(notificationRepository.findByIdAndTeacherId(notificationId, teacherId))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notificationId, teacherId);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_throwsWhenNotFound() {
        when(notificationRepository.findByIdAndTeacherId(notificationId, teacherId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(notificationId, teacherId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");

        verify(notificationRepository, never()).delete(any());
    }

    // ===== countUnreadForTeacher =====

    @Test
    void countUnreadForTeacher_returnsCount() {
        when(notificationRepository.countByTeacherIdAndReadFalse(teacherId)).thenReturn(3L);

        long result = notificationService.countUnreadForTeacher(teacherId);

        assertThat(result).isEqualTo(3L);
        verify(notificationRepository).countByTeacherIdAndReadFalse(teacherId);
    }

    // ===== notifyAreaCompleted =====

    @Test
    void notifyAreaCompleted_createsNotificationForEachTeacher() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);
        Stop stop = buildStop("Area 1");

        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.of(enrollment));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyAreaCompleted(student, stop);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(NotificationType.AREA_COMPLETED);
        assertThat(saved.getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getClassroomId()).isEqualTo(classroomId);
        assertThat(saved.getStudentId()).isEqualTo(studentId);
        assertThat(saved.getMessage())
                .contains("studentUser")
                .contains("Area 1")
                .contains("Test Class");
    }

    @Test
    void notifyAreaCompleted_doesNothingWhenStudentNotEnrolled() {
        User student = buildUser(studentId, "studentUser");
        Stop stop = buildStop("Area 1");

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.empty());

        notificationService.notifyAreaCompleted(student, stop);

        verify(notificationRepository, never()).save(any());
        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
    }

    // ===== notifyGameCompleted =====

    @Test
    void notifyGameCompleted_createsNotificationForEachTeacher() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);

        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.of(enrollment));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyGameCompleted(student);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(NotificationType.GAME_COMPLETED);
        assertThat(saved.getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getClassroomId()).isEqualTo(classroomId);
        assertThat(saved.getStudentId()).isEqualTo(studentId);
        assertThat(saved.getMessage())
                .contains("studentUser")
                .contains("Test Class");
    }

    @Test
    void notifyGameCompleted_doesNothingWhenStudentNotEnrolled() {
        User student = buildUser(studentId, "studentUser");

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.empty());

        notificationService.notifyGameCompleted(student);

        verify(notificationRepository, never()).save(any());
        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
    }

    // ===== notifyInappropriateUsername =====

    @Test
    void notifyInappropriateUsername_createsNotificationForEachTeacher() {
        User student = buildUser(studentId, "studentUser");
        student.setEmail("student@example.com");

        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);

        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.of(enrollment));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyInappropriateUsername(student, "badUsername");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(NotificationType.INAPPROPRIATE_USERNAME);
        assertThat(saved.getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getClassroomId()).isEqualTo(classroomId);
        assertThat(saved.getStudentId()).isEqualTo(studentId);
        assertThat(saved.getMessage())
                .contains("studentUser")
                .contains("student@example.com")
                .contains("badUsername")
                .contains("Test Class");
    }

    @Test
    void notifyInappropriateUsername_doesNothingWhenStudentNotEnrolled() {
        User student = buildUser(studentId, "studentUser");
        student.setEmail("student@example.com");

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.empty());

        notificationService.notifyInappropriateUsername(student, "badUsername");

        verify(notificationRepository, never()).save(any());
        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
    }

    // ===== notifyInappropriateNote =====

    @Test
    void notifyInappropriateNote_createsNotificationForEachTeacher() {
        User student = buildUser(studentId, "studentUser");
        student.setEmail("student@example.com");

        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);

        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.of(enrollment));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyInappropriateNote(student);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(NotificationType.INAPPROPRIATE_NOTE);
        assertThat(saved.getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getClassroomId()).isEqualTo(classroomId);
        assertThat(saved.getStudentId()).isEqualTo(studentId);
        assertThat(saved.getMessage())
                .contains("studentUser")
                .contains("student@example.com")
                .contains("upassende innhold")
                .contains("Test Class");
    }

    @Test
    void notifyInappropriateNote_doesNothingWhenStudentNotEnrolled() {
        User student = buildUser(studentId, "studentUser");
        student.setEmail("student@example.com");

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student))
                .thenReturn(Optional.empty());

        notificationService.notifyInappropriateNote(student);

        verify(notificationRepository, never()).save(any());
        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
    }

    // ===== createStuckStudentNotifications =====

    @Test
    void createStuckStudentNotifications_createsNotificationForStuckStudent() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);
        enrollment.setEnrolledAt(Instant.now().minus(10, ChronoUnit.DAYS));

        UserProgress progress = new UserProgress();
        progress.setUser(student);
        progress.setGameCompleted(false);
        progress.setLastProgressAt(Instant.now().minus(10, ChronoUnit.DAYS));

        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId)))
                .thenReturn(List.of(progress));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
                teacherId,
                classroomId,
                studentId,
                NotificationType.STUDENT_STUCK
        )).thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createStuckStudentNotifications();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(NotificationType.STUDENT_STUCK);
        assertThat(saved.getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getClassroomId()).isEqualTo(classroomId);
        assertThat(saved.getStudentId()).isEqualTo(studentId);
        assertThat(saved.getMessage()).contains("studentUser").contains("7 dager");
    }

    @Test
    void createStuckStudentNotifications_skipsWhenUnreadAlreadyExists() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);
        enrollment.setEnrolledAt(Instant.now().minus(10, ChronoUnit.DAYS));

        UserProgress progress = new UserProgress();
        progress.setUser(student);
        progress.setGameCompleted(false);
        progress.setLastProgressAt(Instant.now().minus(10, ChronoUnit.DAYS));

        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId)))
                .thenReturn(List.of(progress));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
                teacherId,
                classroomId,
                studentId,
                NotificationType.STUDENT_STUCK
        )).thenReturn(true);

        notificationService.createStuckStudentNotifications();

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createStuckStudentNotifications_skipsStudentWhoCompletedGame() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);

        UserProgress progress = new UserProgress();
        progress.setUser(student);
        progress.setGameCompleted(true);

        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId)))
                .thenReturn(List.of(progress));

        notificationService.createStuckStudentNotifications();

        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createStuckStudentNotifications_doesNothingWhenNoActiveEnrollments() {
        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of());

        notificationService.createStuckStudentNotifications();

        verify(userProgressRepository, never()).findByUserIdIn(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createStuckStudentNotifications_skipsStudentWithoutProgress() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);

        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId)))
                .thenReturn(List.of());

        notificationService.createStuckStudentNotifications();

        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createStuckStudentNotifications_skipsStudentWithRecentProgress() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);
        enrollment.setEnrolledAt(Instant.now().minus(10, ChronoUnit.DAYS));

        UserProgress progress = new UserProgress();
        progress.setUser(student);
        progress.setGameCompleted(false);
        progress.setLastProgressAt(Instant.now().minus(2, ChronoUnit.DAYS));

        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId)))
                .thenReturn(List.of(progress));

        notificationService.createStuckStudentNotifications();

        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createStuckStudentNotifications_whenLastProgressAtIsNullUsesEnrollmentDateAndCreatesNotification() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);
        enrollment.setEnrolledAt(Instant.now().minus(10, ChronoUnit.DAYS));

        UserProgress progress = new UserProgress();
        progress.setUser(student);
        progress.setGameCompleted(false);
        progress.setLastProgressAt(null);

        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId)))
                .thenReturn(List.of(progress));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
                .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
                teacherId,
                classroomId,
                studentId,
                NotificationType.STUDENT_STUCK
        )).thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createStuckStudentNotifications();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo(NotificationType.STUDENT_STUCK);
        assertThat(saved.getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getClassroomId()).isEqualTo(classroomId);
        assertThat(saved.getStudentId()).isEqualTo(studentId);
        assertThat(saved.getMessage()).contains("studentUser").contains("7 dager");
    }

    @Test
    void createStuckStudentNotifications_whenLastProgressAtAndEnrolledAtAreNull_shouldSkipStudent() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);
        enrollment.setEnrolledAt(null);

        UserProgress progress = new UserProgress();
        progress.setUser(student);
        progress.setGameCompleted(false);
        progress.setLastProgressAt(null);

        when(enrollmentRepository.findAllActiveWithUserAndClassroom())
                .thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId)))
                .thenReturn(List.of(progress));

        notificationService.createStuckStudentNotifications();

        verify(classroomTeacherRepository, never()).findWithUsersByClassroomId(any());
        verify(notificationRepository, never()).save(any());
    }

    // ===== helpers =====

    private Notification buildNotification(UUID id, boolean read) {
        return Notification.builder()
                .id(id)
                .teacherId(teacherId)
                .classroomId(classroomId)
                .studentId(studentId)
                .type(NotificationType.AREA_COMPLETED)
                .message("test message")
                .read(read)
                .createdAt(Instant.now())
                .build();
    }

    private User buildUser(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }

    private Classroom buildClassroom(UUID id, String title) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setTitle(title);
        return classroom;
    }

    private Enrollment buildEnrollment(User user, Classroom classroom) {
        return Enrollment.builder()
                .id(UUID.randomUUID())
                .user(user)
                .classroom(classroom)
                .enrolledAt(Instant.now())
                .build();
    }

    private Stop buildStop(String name) {
        Stop stop = new Stop();
        stop.setName(name);
        return stop;
    }

    private ClassroomTeacher buildClassroomTeacher(Classroom classroom, User teacher) {
        return ClassroomTeacher.builder()
                .classroom(classroom)
                .user(teacher)
                .isOwner(true)
                .build();
    }
}