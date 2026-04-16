package org.ntnu.backend.game.model;

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
    private Integer id;

    @NotNull
    private String imageUrl;

    @NotNull
    private Boolean isCorrect;

    @NotBlank
    private String explanation;
}