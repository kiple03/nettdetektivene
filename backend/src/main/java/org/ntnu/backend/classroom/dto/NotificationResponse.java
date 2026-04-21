package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * Data Transfer Object for notifaction of classroom. 
 */
@Value
@Builder
public class NotificationResponse {
    String type;
    String message;
    String classroomId;
    String studentUsername;
    Instant timestamp;
}
