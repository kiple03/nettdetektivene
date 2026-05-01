package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.ntnu.backend.classroom.model.Notification;
import org.ntnu.backend.classroom.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Notification} entities, providing query methods scoped to a teacher
 * and optionally a classroom.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Returns all notifications for a teacher, newest first.
     *
     * @param teacherId the teacher's ID
     * @return notifications ordered by creation time descending
     */
    List<Notification> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    /**
     * Returns all notifications for a teacher within a specific classroom, newest first.
     *
     * @param teacherId   the teacher's ID
     * @param classroomId the classroom's ID
     * @return notifications ordered by creation time descending
     */
    List<Notification> findByTeacherIdAndClassroomIdOrderByCreatedAtDesc(UUID teacherId, UUID classroomId);

    /**
     * Finds a single notification by its ID, verifying it belongs to the given teacher.
     *
     * @param id        the notification ID
     * @param teacherId the teacher's ID
     * @return the notification if found and owned by the teacher
     */
    Optional<Notification> findByIdAndTeacherId(UUID id, UUID teacherId);

    /**
     * Counts unread notifications for a teacher.
     *
     * @param teacherId the teacher's ID
     * @return number of unread notifications
     */
    long countByTeacherIdAndReadFalse(UUID teacherId);

    /**
     * Checks whether an unread notification of the given type already exists for the
     * teacher/classroom/student combination, used to avoid duplicate notifications.
     *
     * @param teacherId   the teacher's ID
     * @param classroomId the classroom's ID
     * @param studentId   the student's ID
     * @param type        the notification type
     * @return {@code true} if such an unread notification exists
     */
    boolean existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
            UUID teacherId,
            UUID classroomId,
            UUID studentId,
            NotificationType type);
}
