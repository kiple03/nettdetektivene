package org.ntnu.backend.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public final class PasswordStrengthOption implements Option {
    
    @NotNull private Integer id;
    @Min(1) private int minLength;
    @JsonProperty("requireDigit") private boolean requireDigit;
    @JsonProperty("requireUpper") private boolean requireUpper;
    @JsonProperty("requireLower") private boolean requireLower;
    @JsonProperty("requireSymbol") private boolean requireSymbol;

    private String imageUrl;

    //For server only
    @NotBlank private String successFeedback;
    @NotBlank private String failureFeedback;

}
