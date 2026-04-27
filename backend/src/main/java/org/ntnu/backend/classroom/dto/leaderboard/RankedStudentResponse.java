package org.ntnu.backend.classroom.dto.leaderboard;

public record RankedStudentResponse(
    int rank,
    LeaderboardStudentResponse student
) {}
