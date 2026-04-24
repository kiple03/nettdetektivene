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

import org.ntnu.backend.classroom.model.Classroom;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private ClassroomTeacherRepository classroomTeacherRepository;
    @Mock private UserProgressRepository userProgressRepository;

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
            notificationMapper);
        
    }

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

    // ===== createNotification =====

    @Test
    void createNotification_savesAndReturnsResponse() {
        Notification saved = buildNotification(notificationId, false);
        when(notificationRepository.save(any())).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(
            teacherId, classroomId, studentId, NotificationType.AREA_COMPLETED, "test message");

        assertThat(response.getId()).isEqualTo(notificationId);
        assertThat(response.isRead()).isFalse();
        assertThat(response.getType()).isEqualTo(NotificationType.AREA_COMPLETED);
        verify(notificationRepository).save(any());
    }

    // ===== getNotificationsForTeacher =====

    @Test
    void getNotificationsForTeacher_returnsAllForTeacher() {
        Notification n1 = buildNotification(UUID.randomUUID(), false);
        Notification n2 = buildNotification(UUID.randomUUID(), true);
        when(notificationRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId))
            .thenReturn(List.of(n1, n2));

        List<NotificationResponse> result = notificationService.getNotificationsForTeacher(teacherId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getNotificationsForTeacher_filteredByClassroom() {
        Notification n = buildNotification(UUID.randomUUID(), false);
        when(notificationRepository.findByTeacherIdAndClassroomIdOrderByCreatedAtDesc(teacherId, classroomId))
            .thenReturn(List.of(n));

        List<NotificationResponse> result = notificationService.getNotificationsForTeacher(teacherId, classroomId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClassroomId()).isEqualTo(classroomId);
    }

    // ===== markAsRead =====

    @Test
    void markAsRead_setsReadAndReturnsResponse() {
        Notification notification = buildNotification(notificationId, false);
        when(notificationRepository.findByIdAndTeacherId(notificationId, teacherId))
            .thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(notificationId, teacherId);

        assertThat(response.isRead()).isTrue();
    }

    @Test
    void markAsRead_throwsWhenNotFound() {
        when(notificationRepository.findByIdAndTeacherId(notificationId, teacherId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(notificationId, teacherId))
            .isInstanceOf(ResponseStatusException.class);
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
            .isInstanceOf(ResponseStatusException.class);
    }

    // ===== countUnreadForTeacher =====

    @Test
    void countUnreadForTeacher_returnsCount() {
        when(notificationRepository.countByTeacherIdAndReadFalse(teacherId)).thenReturn(3L);

        assertThat(notificationService.countUnreadForTeacher(teacherId)).isEqualTo(3L);
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

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student)).thenReturn(Optional.of(enrollment));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
            .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyAreaCompleted(student, stop);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.AREA_COMPLETED);
        assertThat(saved.getTeacherId()).isEqualTo(teacherId);
        assertThat(saved.getMessage()).contains("studentUser").contains("Area 1");
    }

    @Test
    void notifyAreaCompleted_doesNothingWhenStudentNotEnrolled() {
        User student = buildUser(studentId, "studentUser");
        Stop stop = buildStop("Area 1");
        when(enrollmentRepository.findByUserAndLeftAtIsNull(student)).thenReturn(Optional.empty());

        notificationService.notifyAreaCompleted(student, stop);

        verify(notificationRepository, never()).save(any());
    }

    // ===== notifyGameCompleted =====

    @Test
    void notifyGameCompleted_createsNotificationForEachTeacher() {
        User student = buildUser(studentId, "studentUser");
        Classroom classroom = buildClassroom(classroomId, "Test Class");
        Enrollment enrollment = buildEnrollment(student, classroom);
        User teacher = buildUser(teacherId, "teacherUser");
        ClassroomTeacher classroomTeacher = buildClassroomTeacher(classroom, teacher);

        when(enrollmentRepository.findByUserAndLeftAtIsNull(student)).thenReturn(Optional.of(enrollment));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
            .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyGameCompleted(student);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.GAME_COMPLETED);
        assertThat(saved.getMessage()).contains("studentUser");
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

        when(enrollmentRepository.findAllActiveWithUserAndClassroom()).thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId))).thenReturn(List.of(progress));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
            .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
            teacherId, classroomId, studentId, NotificationType.STUDENT_STUCK)).thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.createStuckStudentNotifications();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.STUDENT_STUCK);
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

        when(enrollmentRepository.findAllActiveWithUserAndClassroom()).thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(List.of(studentId))).thenReturn(List.of(progress));
        when(classroomTeacherRepository.findWithUsersByClassroomId(classroomId))
            .thenReturn(List.of(classroomTeacher));
        when(notificationRepository.existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
            teacherId, classroomId, studentId, NotificationType.STUDENT_STUCK)).thenReturn(true);

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

        when(enrollmentRepository.findAllActiveWithUserAndClassroom()).thenReturn(List.of(enrollment));
        when(userProgressRepository.findByUserIdIn(any())).thenReturn(List.of(progress));

        notificationService.createStuckStudentNotifications();

        verify(notificationRepository, never()).save(any());
    }

    // ===== helpers =====

    private User buildUser(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
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
