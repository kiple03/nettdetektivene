package org.ntnu.backend.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.game.dto.MultipleChoiceOptionResponse;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;

/**
 * A single option in a {@link LevelType#MULTIPLE_CHOICE} question. Stored as JSON
 * inside {@link Question#getOptionContent()} and deserialized by {@link OptionParser}.
 *
 * <p>Exactly one option per question must have {@code isCorrect = true},
 * enforced by {@link OptionValidator}.
 *
 * <p>The {@code isCorrect} and {@code explanation} fields are server-only,
 * they are used by validators but never included in API responses.
 * The corresponding response DTO ({@link MultipleChoiceOptionResponse})
 * only exposes {@code id}, {@code text}, and {@code imageUrl}.
 *
 * @see Option
 * @see MultipleChoiceOptionResponse
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class MultipleChoiceOption implements Option {

    @NotNull
    private Integer id;

    @NotBlank
    private String text;

    private String imageUrl;

    @JsonProperty("isCorrect")
    private boolean isCorrect;

    @NotBlank
    private String explanation;
}