package org.ntnu.backend.teacher.dto;

public record StudentSummary(
        String userId,
        String username,
        long medalsEarned,
        long levelsCompleted
) {}
