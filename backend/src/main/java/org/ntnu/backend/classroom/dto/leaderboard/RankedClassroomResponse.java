package org.ntnu.backend.classroom.dto.leaderboard;

/**
 * A classroom entry on the leaderboard with its computed rank position.
 *
 * @param rank      1-based position on the leaderboard
 * @param classroom classroom details and score
 */
public record RankedClassroomResponse(
    int rank,
    LeaderboardClassroomResponse classroom
) {}
