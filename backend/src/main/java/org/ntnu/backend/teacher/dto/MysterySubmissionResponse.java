package org.ntnu.backend.teacher.dto;

public record MysterySubmissionResponse(
        String id,
        String studentUsername,
        String content,
        String imageUrl,
        String status,
        boolean featured,
        String submittedAt,
        String reviewedAt
) {}
