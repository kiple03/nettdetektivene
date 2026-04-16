package org.ntnu.backend.game.dto;

import org.ntnu.backend.game.model.enums.LevelType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data Transfer Object for level in a stop")
public record LevelDto(
    int id,

    @Schema(description = "Title of level")
    @NotBlank
    String title,

    @Schema(description = "A description of the level")
    String description,

    @Schema(description = "The structure of the level")
    @NotNull
    LevelType levelType
    //List<Questions> not included to avoid too much info through API
) {}
