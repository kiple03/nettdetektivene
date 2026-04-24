package org.ntnu.backend.classroom.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int STUCK_DAYS_THRESHOLD = 7;

    private final NotificationRepository notificationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final UserProgressRepository userProgressRepository;
    private final NotificationMapper notificationMapper;

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

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForTeacher(UUID teacherId) {
        return notificationRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForTeacher(UUID teacherId, UUID classroomId) {
        return notificationRepository.findByTeacherIdAndClassroomIdOrderByCreatedAtDesc(teacherId, classroomId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID teacherId) {
        Notification notification = notificationRepository.findByIdAndTeacherId(notificationId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        notification.setRead(true);
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public void deleteNotification(UUID notificationId, UUID teacherId) {
        Notification notification = notificationRepository.findByIdAndTeacherId(notificationId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public long countUnreadForTeacher(UUID teacherId) {
        return notificationRepository.countByTeacherIdAndReadFalse(teacherId);
    }

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyInappropriateUsername(User student, String attemptedUsername) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserAndLeftAtIsNull(student);
        if (enrollmentOpt.isEmpty()) {
            return;
        }
        Enrollment enrollment = enrollmentOpt.get();
        UUID classroomId = enrollment.getClassroom().getId();
        String message = student.getUsername() + " forsøkte å bruke brukernavnet \"" + attemptedUsername
                + "\" som inneholder upassende innhold i " + enrollment.getClassroom().getTitle() + ".";
        classroomTeacherRepository.findWithUsersByClassroomId(classroomId).forEach(teacher ->
                createNotification(teacher.getUser().getId(), classroomId, student.getId(),
                        NotificationType.INAPPROPRIATE_USERNAME, message));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyInappropriateNote(User student) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserAndLeftAtIsNull(student);
        if (enrollmentOpt.isEmpty()) {
            return;
        }
        Enrollment enrollment = enrollmentOpt.get();
        UUID classroomId = enrollment.getClassroom().getId();
        String message = student.getUsername() + " lagret et notat med upassende innhold i "
                + enrollment.getClassroom().getTitle() + ".";
        classroomTeacherRepository.findWithUsersByClassroomId(classroomId).forEach(teacher ->
                createNotification(teacher.getUser().getId(), classroomId, student.getId(),
                        NotificationType.INAPPROPRIATE_NOTE, message));
    }

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
