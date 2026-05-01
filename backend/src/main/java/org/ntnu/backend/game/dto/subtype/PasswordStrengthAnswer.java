package org.ntnu.backend.game.dto.subtype;

import lombok.Getter;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;

/** Answer payload for password-strength question types. */
@Getter
public class PasswordStrengthAnswer extends SubmitAnswerRequest {
    /** The password submitted by the student for strength evaluation. */
    private String password;
}
