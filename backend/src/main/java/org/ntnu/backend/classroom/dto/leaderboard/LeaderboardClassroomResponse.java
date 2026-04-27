package org.ntnu.backend.classroom.dto.leaderboard;


public record LeaderboardClassroomResponse(
    String id,
    String name,
    int totalScore,
    int avgScore
) {}
