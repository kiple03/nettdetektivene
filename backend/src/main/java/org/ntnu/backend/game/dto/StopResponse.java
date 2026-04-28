package org.ntnu.backend.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data Transfer Object of game stop")
public record StopResponse(
    int id,
    
    @Schema(description = "The name of the stop")
    String name,

//    @Schema(description = "The levels in the stop")
//    List<LevelResponse> levels

    boolean locked
) {}
