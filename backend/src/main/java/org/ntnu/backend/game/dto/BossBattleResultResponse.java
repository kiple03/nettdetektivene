package org.ntnu.backend.game.dto;

import java.time.Instant;

/**
 * Response DTO returned after a boss battle is completed.
 *
 * @param won             whether the player defeated the boss
 * @param timeSeconds     total seconds the battle lasted
 * @param playerHpRemaining HP left when the battle ended
 * @param completedAt     timestamp of when the battle finished
 */
public record BossBattleResultResponse(
    boolean won,
    int timeSeconds,
    int playerHpRemaining,
    Instant completedAt
) {}
