package org.ntnu.backend.game.dto;

import org.ntnu.backend.game.model.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;

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
