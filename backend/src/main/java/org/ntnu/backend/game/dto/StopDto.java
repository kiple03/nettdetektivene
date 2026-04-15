package org.ntnu.backend.level.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data Transfer Object of game stop")
public record StopDto (
    @NotBlank
    int id,
    
    @Schema(description = "The name of the stop")
    @NotBlank
    String name,

    @Schema(description = "The levels in the stop")
    @NotNull
    List<LevelDto> levels
) {}
