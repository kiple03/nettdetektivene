package org.ntnu.backend.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.ntnu.backend.classroom.model.Notification;
import org.ntnu.backend.classroom.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    List<Notification> findByTeacherIdAndClassroomIdOrderByCreatedAtDesc(UUID teacherId, UUID classroomId);

    Optional<Notification> findByIdAndTeacherId(UUID id, UUID teacherId);

    long countByTeacherIdAndReadFalse(UUID teacherId);

    boolean existsByTeacherIdAndClassroomIdAndStudentIdAndTypeAndReadFalse(
            UUID teacherId,
            UUID classroomId,
            UUID studentId,
            NotificationType type);
}
