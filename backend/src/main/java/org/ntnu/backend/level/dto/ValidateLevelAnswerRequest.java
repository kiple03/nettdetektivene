package org.ntnu.backend.level.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidateLevelAnswerRequest {
    @NotBlank(message = "Option must be selected")
    private String optionId;

    public string getOptionId() {
        return optionId;
    }

    public void setAnswer(String optionId) {
        this.optionId = optionId;
    }
}