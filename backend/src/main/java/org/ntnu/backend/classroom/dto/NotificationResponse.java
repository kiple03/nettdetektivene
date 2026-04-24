package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import java.util.UUID;

import org.ntnu.backend.classroom.model.enums.NotificationType;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationResponse {
    UUID id;
    UUID teacherId;
    UUID classroomId;
    UUID studentId;
    NotificationType type;
    String message;
    boolean read;
    Instant createdAt;
}
