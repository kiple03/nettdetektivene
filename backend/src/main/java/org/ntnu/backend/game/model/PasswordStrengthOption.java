package org.ntnu.backend.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Option that validates a password against strength requirements.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public final class PasswordStrengthOption implements Option {

    /** Unique identifier for this option. */
    @NotNull private Integer id;

    /** Minimum required password length. */
    @Min(1) private int minLength;

    /** Whether a digit is required. */
    @JsonProperty("requireDigit") private boolean requireDigit;

    /** Whether an uppercase letter is required. */
    @JsonProperty("requireUpper") private boolean requireUpper;

    /** Whether a lowercase letter is required. */
    @JsonProperty("requireLower") private boolean requireLower;

    /** Whether a symbol is required. */
    @JsonProperty("requireSymbol") private boolean requireSymbol;

    private String imageUrl;

    /** Feedback shown to the student on correct answer. */
    @NotBlank private String successFeedback;

    /** Feedback shown to the student on incorrect answer. */
    @NotBlank private String failureFeedback;

}
