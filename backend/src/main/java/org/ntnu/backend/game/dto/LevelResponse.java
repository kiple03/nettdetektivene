package org.ntnu.backend.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ntnu.backend.game.model.enums.LevelType;

/**
 * Response DTO representing a single level within a game stop.
 *
 * @param id       unique identifier of the level
 * @param title    display title of the level
 * @param description brief explanation of the level's content or goal
 * @param stopName name of the stop this level belongs to
 * @param levelType structural type that determines how the level is rendered/played
 * @param order    position of the level within its stop (ascending)
 * @param locked   whether the user is currently allowed to play this level
 */
@Schema(description = "Data Transfer Object for level in a stop")
public record LevelResponse(

    @Schema(description = "The level id")
    int id,

    @Schema(description = "Title of level")
    String title,

    @Schema(description = "A description of the level")
    String description,

    @Schema(description = "Name of the stop this level belongs to")
    String stopName,

    @Schema(description = "The structure of the level")
    LevelType levelType,

    @Schema(description = "The order of the level")
    int order,

    @Schema(description = "If the user is able to play this level")
    boolean locked
) {}
