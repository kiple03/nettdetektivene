package org.ntnu.backend.game.dto.subtypes;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;

import lombok.Getter;

@Getter
public class PasswordStrengthAnswer extends SubmitAnswerRequest {
    private String password;
}
