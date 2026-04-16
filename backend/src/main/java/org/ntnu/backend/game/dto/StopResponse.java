package org.ntnu.backend.game.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data Transfer Object of game stop")
public record StopResponse(
    int id,
    
    @Schema(description = "The name of the stop")
    String name,

    @Schema(description = "The levels in the stop")
    List<LevelResponse> levels
) {}
