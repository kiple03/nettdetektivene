package org.ntnu.backend.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class MultipleChoiceOption implements Option {

    @NotNull
    private int id;

    @NotBlank
    private String text;

    @NotBlank
    private String imageUrl;

    @JsonProperty("isCorrect")
    private boolean isCorrect;

    @NotBlank
    private String explanation;
}