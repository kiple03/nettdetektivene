package org.ntnu.backend.classroom.dto.leaderboard;

/**
 * A student entry on the leaderboard together with their computed rank position.
 *
 * @param rank    1-based position in the leaderboard ordering
 * @param student student details and score data
 */
public record RankedStudentResponse(
    int rank,
    LeaderboardStudentResponse student
) {}
