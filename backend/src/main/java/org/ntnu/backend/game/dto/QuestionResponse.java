package org.ntnu.backend.game.dto;

import org.ntnu.backend.game.model.enums.LevelType;

public record QuestionResponse(
    int id,
    LevelType levelType,
    String questionText,
    String optionContent
) {}
