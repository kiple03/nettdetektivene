package org.ntnu.backend.level.dto;

public class ValidateLevelAnswerResponse {
    private final boolean isCorrect;
    private final String feedback;
    private final boolean unlockedNextLevel;

    public ValidateLevelAnswerResponse(boolean isCorrect, String feedback, boolean unlockedNextLevel) {
        this.isCorrect = isCorrect;
        this.feedback = feedback;
        this.unlockedNextLevel = unlockedNextLevel;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public String getFeedback() {
        return feedback;
    }

    public boolean isUnlockedNextLevel() {
        return unlockedNextLevel;
    }
}