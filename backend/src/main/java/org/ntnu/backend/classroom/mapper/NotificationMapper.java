package org.ntnu.backend.classroom.mapper;

import java.util.UUID;

import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.model.Notification;
import org.ntnu.backend.classroom.model.enums.NotificationType;
import org.springframework.stereotype.Component;

/** Maps between {@link Notification} entities and their DTOs. */
@Component
public class NotificationMapper {

    /**
     * Builds a new, unread {@link Notification} entity from its constituent fields.
     *
     * @param teacherId   ID of the teacher who receives the notification
     * @param classroomId ID of the classroom the notification belongs to
     * @param studentId   ID of the student who triggered the notification
     * @param type        category of the notification event
     * @param message     human-readable notification text
     * @return a new {@link Notification} with {@code read} set to {@code false}
     */
    public Notification toEntity(
            UUID teacherId,
            UUID classroomId,
            UUID studentId,
            NotificationType type,
            String message) {
        return Notification.builder()
                .teacherId(teacherId)
                .classroomId(classroomId)
                .studentId(studentId)
                .type(type)
                .message(message)
                .read(false)
                .build();
    }

    /**
     * Converts a {@link Notification} entity to a {@link NotificationResponse} DTO.
     *
     * @param notification the entity to convert
     * @return a fully populated response DTO
     */
    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .teacherId(notification.getTeacherId())
                .classroomId(notification.getClassroomId())
                .studentId(notification.getStudentId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
