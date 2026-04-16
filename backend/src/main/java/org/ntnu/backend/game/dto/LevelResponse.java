package org.ntnu.backend.game.dto;

import org.ntnu.backend.game.model.enums.LevelType;

public record LevelResponse(
    int id,
    String title,
    String description,
    LevelType levelType,
    boolean locked
) {}
