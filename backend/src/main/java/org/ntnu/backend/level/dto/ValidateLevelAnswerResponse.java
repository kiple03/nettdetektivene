package org.ntnu.backend.level.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidateLevelAnswerResponse {
    private final boolean isCorrect;
    private final String feedback;
    private final boolean unlockedNextLevel;
}