package org.ntnu.backend.game.dto;

import java.time.Instant;

public record BossBattleResultResponse(
    boolean won,
    int timeSeconds,
    int playerHpRemaining,
    Instant completedAt
) {}
