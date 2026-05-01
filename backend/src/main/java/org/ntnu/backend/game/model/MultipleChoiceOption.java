package org.ntnu.backend.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.game.dto.optionresponse.MultipleChoiceOptionResponse;
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

    /** Unique identifier for this option within its question. */
    @NotNull
    private Integer id;

    /** Display text shown to the student. */
    private String text;

    /** Optional image URL shown alongside the text. */
    private String imageUrl;

    /** Alt text describing the image content for accessibility.*/
    private String imageAlt;

    /** Response images for news **/
    private String responseImageUrl;

  /** Whether this option is the correct answer; never exposed in API responses. */
    @JsonProperty("isCorrect")
    private boolean isCorrect;

    /** Explanation shown after the student answers; never exposed before submission. */
    @NotBlank
    private String explanation;

  public MultipleChoiceOption(
      Integer id,
      String text,
      String imageUrl,
      boolean isCorrect,
      String explanation
  ) {
    this.id = id;
    this.text = text;
    this.imageUrl = imageUrl;
    this.isCorrect = isCorrect;
    this.explanation = explanation;
  }

  public MultipleChoiceOption(
      Integer id,
      String text,
      String imageUrl,
      String responseImageUrl,
      boolean isCorrect,
      String explanation
  ) {
    this.id = id;
    this.text = text;
    this.imageUrl = imageUrl;
    this.responseImageUrl = responseImageUrl;
    this.isCorrect = isCorrect;
    this.explanation = explanation;
  }

}