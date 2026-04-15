package org.ntnu.backend.teacher.dto;

public record NotificationResponse(
        String type,
        String message,
        String classroomId,
        String studentUsername,
        String timestamp
) {}
