package org.ntnu.backend.teacher.dto;

public record StopSummary(
        String id,
        String title,
        String description,
        int displayOrder
) {}
