package org.ntnu.backend.classroom.mapper;

import java.util.UUID;

import org.ntnu.backend.classroom.dto.NotificationResponse;
import org.ntnu.backend.classroom.model.Notification;
import org.ntnu.backend.classroom.model.enums.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

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
