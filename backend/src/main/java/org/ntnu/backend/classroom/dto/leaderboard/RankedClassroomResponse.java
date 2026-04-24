package org.ntnu.backend.classroom.dto.leaderboard;

public record RankedClassroomResponse(
    int rank,
    LeaderboardClassroomResponse classroom
) {}
