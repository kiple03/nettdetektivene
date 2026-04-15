package org.ntnu.backend.teacher.dto;

public record ClassroomSummary(
        String id,
        String title,
        String description,
        String joinCode,
        String createdAt,
        long studentCount
) {}
