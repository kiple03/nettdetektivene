package org.ntnu.backend.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO representing a game stop (a node/checkpoint in the game map).
 *
 * @param id     unique identifier of the stop
 * @param name   display name of the stop
 * @param locked whether the stop is locked for the user
 */
@Schema(description = "Data Transfer Object of game stop")
public record StopResponse(
    int id,

    @Schema(description = "The name of the stop")
    String name,

    boolean locked
) {}
