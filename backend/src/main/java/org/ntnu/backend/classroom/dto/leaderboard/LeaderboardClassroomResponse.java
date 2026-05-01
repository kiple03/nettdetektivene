package org.ntnu.backend.classroom.dto.leaderboard;

/**
 * Leaderboard entry for a single classroom, used in cross-classroom rankings.
 *
 * @param id         unique classroom identifier
 * @param name       display name of the classroom
 * @param totalScore sum of all student scores in the classroom
 * @param avgScore   average score per student in the classroom
 */
public record LeaderboardClassroomResponse(
    String id,
    String name,
    int totalScore,
    int avgScore
) {}
