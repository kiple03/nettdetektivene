package org.ntnu.backend.game.dto.bossbattle;

import jakarta.validation.constraints.Min;

/**
 * Request DTO for persisting the outcome of a completed boss battle.
 *
 * @param won               whether the player defeated the boss
 * @param timeSeconds       total seconds the battle lasted (≥ 0)
 * @param playerHpRemaining HP remaining when the battle ended (≥ 0)
 */
public record BossBattleSaveRequest(
    boolean won,
    @Min(0) int timeSeconds,
    @Min(0) int playerHpRemaining
) {}
