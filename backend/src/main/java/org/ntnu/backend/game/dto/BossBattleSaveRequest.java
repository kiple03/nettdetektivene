package org.ntnu.backend.game.dto;

import jakarta.validation.constraints.Min;

public record BossBattleSaveRequest(
    boolean won,
    @Min(0) int timeSeconds,
    @Min(0) int playerHpRemaining
) {}
