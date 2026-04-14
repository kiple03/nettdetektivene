package org.ntnu.backend.level.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateLevelAnswerRequest {

    @NotBlank(message = "Du må velge ett alternativ")
    private String optionId;
}