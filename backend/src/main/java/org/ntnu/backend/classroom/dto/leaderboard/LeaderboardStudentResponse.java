package org.ntnu.backend.classroom.dto.leaderboard;

/**
 * A student entry in the classroom leaderboard, containing the student's display name and total score.
 *
 * @param username the student's display name
 * @param score    the student's total accumulated score
 */
public record LeaderboardStudentResponse(
    String username,
    int score
) {}

