package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import java.util.UUID;

import org.ntnu.backend.classroom.model.enums.NotificationType;

import lombok.Builder;
import lombok.Value;

/**
 * DTO representing a notification sent to a teacher about activity in their classroom
 * (e.g. student join requests, mystery submissions).
 */
@Value
@Builder
public class NotificationResponse {
    /** Unique identifier of this notification. */
    UUID id;
    /** Teacher who received the notification. */
    UUID teacherId;
    /** Classroom the notification is associated with. */
    UUID classroomId;
    /** Student who triggered the notification, if applicable. */
    UUID studentId;
    /** Category of event that generated this notification. */
    NotificationType type;
    /** Human-readable description of the event. */
    String message;
    /** Whether the teacher has already viewed this notification. */
    boolean read;
    /** When the notification was created. */
    Instant createdAt;
}
