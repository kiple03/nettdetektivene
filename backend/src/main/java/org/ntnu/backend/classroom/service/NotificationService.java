package org.ntnu.backend.classroom.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.mapper.NotificationMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Manages teacher notifications for student activity events and scheduled stuck-student checks. */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int STUCK_DAYS_THRESHOLD = 7;

    private final NotificationRepository notificationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final UserProgressRepository userProgressRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Persists a new notification directed at a teacher.
     *
     * @param teacherId   recipient teacher ID
     * @param classroomId classroom the event belongs to
     * @param studentId   student who triggered the event
     * @param type        notification category
     * @param message     human-readable description
     * @return the saved notification as a response DTO
     */
    @Transactional
    public NotificationResponse createNotification(
            UUID teacherId,
            UUID classroomId,
            UUID studentId,
            NotificationType type,
            String message) {
        Notification notification = notificationRepository.save(
                notificationMapper.toEntity(teacherId, classroomId, studentId, type, message));
        return notificationMapper.toResponse(notification);
    }

    /**
     * Returns all notifications for a teacher, newest first.
     *
     * @param teacherId the teacher's ID
     * @return list of notification response DTOs
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForTeacher(UUID teacherId) {
        return notificationRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    /**
     * Returns notifications for a teacher filtered by classroom, newest first.
     *
     * @param teacherId   the teacher's ID
     * @param classroomId classroom to filter by
     * @return list of notification response DTOs
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForTeacher(UUID teacherId, UUID classroomId) {
        return notificationRepository.findByTeacherIdAndClassroomIdOrderByCreatedAtDesc(teacherId, classroomId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    /**
     * Marks a notification as read, verifying ownership by the given teacher.
     *
     * @param notificationId notification to mark
     * @param teacherId      must own the notification
     * @return updated notification DTO
     * @throws ResponseStatusException 404 if not found or not owned by teacher
     */
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID teacherId) {
        Notification notification = notificationRepository.findByIdAndTeacherId(notificationId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        notification.setRead(true);
        return notificationMapper.toResponse(notification);
    }

    /**
     * Deletes a notification, verifying ownership by the given teacher.
     *
     * @param notificationId notification to delete
     * @param teacherId      must own the notification
     * @throws ResponseStatusException 404 if not found or not owned by teacher
     */
    @Transactional
    public void deleteNotification(UUID notificationId, UUID teacherId) {
        Notification notification = notificationRepository.findByIdAndTeacherId(notificationId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        notificationRepository.delete(notification);
    }

    /**
     * Returns the number of unread notifications for a teacher.
     *
     * @param teacherId the teacher's ID
     * @return unread notification count
     */
    @Transactional(readOnly = true)
    public long countUnreadForTeacher(UUID teacherId) {
        return notificationRepository.countByTeacherIdAndReadFalse(teacherId);
    }

    /**
     * Notifies all teachers of a classroom that a student completed a stop/area.
     * Does nothing if the student is not currently enrolled.
     *
     * @param student       the student who completed the area
     * @param completedStop the stop that was completed
     */
    @Transactional
    public void notifyAreaCompleted(User student, Stop completedStop) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserAndLeftAtIsNull(student);
        if (enrollmentOpt.isEmpty()) {
            return;
        }

        Enrollment enrollment = enrollmentOpt.get();
        UUID classroomId = enrollment.getClassroom().getId();
        String classroomTitle = enrollment.getClassroom().getTitle();
        String message = student.getUsername() + " fullforte omradet " + completedStop.getName()
            + " i " + classroomTitle + ".";

        classroomTeacherRepository.findWithUsersByClassroomId(classroomId).forEach(teacher ->
                createNotification(teacher.getUser().getId(), classroomId, student.getId(), NotificationType.AREA_COMPLETED, message));
    }

    /**
     * Notifies all teachers of a classroom that a student finished the entire game.
     * Does nothing if the student is not currently enrolled.
     *
     * @param student the student who completed the game
     */
    @Transactional
    public void notifyGameCompleted(User student) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserAndLeftAtIsNull(student);
        if (enrollmentOpt.isEmpty()) {
            return;
        }

        Enrollment enrollment = enrollmentOpt.get();
        UUID classroomId = enrollment.getClassroom().getId();
        String classroomTitle = enrollment.getClassroom().getTitle();
        String message = student.getUsername() + " har fullfort hele spillet i " + classroomTitle + "!";

        classroomTeacherRepository.findWithUsersByClassroomId(classroomId).forEach(teacher ->
                createNotification(teacher.getUser().getId(), classroomId, student.getId(), NotificationType.GAME_COMPLETED, message));
    }

    /**
     * Alerts all classroom teachers that a student attempted to use an inappropriate username.
     * Runs in a new transaction so the alert is persisted even if the outer transaction rolls back.
     *
     * @param student           the student who made the attempt
     * @param attemptedUsername the rejected username
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyInappropriateUsername(User student, String attemptedUsername) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserAndLeftAtIsNull(student);
        if (enrollmentOpt.isEmpty()) {
            return;
        }
        Enrollment enrollment = enrollmentOpt.get();
        UUID classroomId = enrollment.getClassroom().getId();
        String message = student.getUsername() + " (" + student.getEmail() + ") forsøkte å bruke brukernavnet \""
                + attemptedUsername + "\" som inneholder upassende innhold i " + enrollment.getClassroom().getTitle() + ".";
        classroomTeacherRepository.findWithUsersByClassroomId(classroomId).forEach(teacher ->
                createNotification(teacher.getUser().getId(), classroomId, student.getId(),
                        NotificationType.INAPPROPRIATE_USERNAME, message));
    }

    /**
     * Alerts all classroom teachers that a student saved a note with inappropriate content.
     * Runs in a new transaction so the alert is persisted even if the outer transaction rolls back.
     *
     * @param student the student who wrote the note
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyInappropriateNote(User student) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserAndLeftAtIsNull(student);
        if (enrollmentOpt.isEmpty()) {
            return;
        }
        Enrollment enrollment = enrollmentOpt.get();
        UUID classroomId = enrollment.getClassroom().getId();
        String message = student.getUsername() + " (" + student.getEmail() + ") lagret et notat med upassende innhold i "
                + enrollment.getClassroom().getTitle() + ".";
        classroomTeacherRepository.findWithUsersByClassroomId(classroomId).forEach(teacher ->
                createNotification(teacher.getUser().getId(), classroomId, student.getId(),
                        NotificationType.INAPPROPRIATE_NOTE, message));
    }

    /**
     * Scheduled daily at 06:00. For each actively enrolled student who has had no progress
     * for {@value #STUCK_DAYS_THRESHOLD} days, creates a {@code STUDENT_STUCK} notification
     * for each of their classroom teachers — but only if no unread notification of that type
     * already exists for that teacher/classroom/student combination.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void createStuckStudentNotifications() {
        Instant cutoff = Instant.now().minus(STUCK_DAYS_THRESHOLD, ChronoUnit.DAYS);
        List<Enrollment> activeEnrollments = enrollmentRepository.findAllActiveWithUserAndClassroom();
        if (activeEnrollments.isEmpty()) {
            return;
        }

        List<UUID> studentIds = activeEnrollments.stream()
                .map(e -> e.getUser().getId())
                .distinct()
                .toList();

        Map<UUID, UserProgress> progressByStudent = userProgressRepository.findByUserIdIn(studentIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));

        for (Enrollment enrollment : activeEnrollments) {
            UUID studentId = enrollment.getUser().getId();
            UserProgress progress = progressByStudent.get(studentId);
            if (progress == null || progress.isGameCompleted()) {
                continue;
            }

            Instant lastProgressAt = progress.getLastProgressAt();
            Instant fallbackAt = enrollment.getEnrolledAt();
            Instant referenceTime = lastProgressAt != null ? lastProgressAt : fallbackAt;
            if (referenceTime == null || referenceTime.isAfter(cutoff)) {
                continue;
            }

            UUID classroomId = enrollment.getClassroom().getId();
            String message = enrollment.getUser().getUsername() + " har ikke hatt fremgang pa over 7 dager.";

            classroomTeacherRepository.findWithUsersByClassroomId(classroomId).forEach(teacher -> {
                UUID teacherId = teacher.getUser().getId();
                boolean hasUnread = notificationRepository
                        .existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
                                teacherId,
                                classroomId,
                                studentId,
                                NotificationType.STUDENT_STUCK);
                if (!hasUnread) {
                    createNotification(teacherId, classroomId, studentId, NotificationType.STUDENT_STUCK, message);
                }
            });
        }
    }

}
