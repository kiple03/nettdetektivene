package org.ntnu.backend.classroom.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationResponse {
    String type;
    String message;
    String classroomId;
    String studentUsername;
    Instant timestamp;
}
