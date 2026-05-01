package org.ntnu.backend.game.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An option in a classify-items question, representing a single item to be categorized.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class ClassifyItemsOption implements Option {

  /** Unique identifier for this option within the question. */
  @NotNull
  private String key;

  /** Display text of the item to classify. */
  @NotBlank
  private String text;

  /** All available category labels the student can choose from. */
  @NotNull
  private List<String> labels;

  /** The label that constitutes the correct answer. */
  @NotBlank
  private String correctLabel;

  /** Explanation shown after answering. */
  @NotBlank
  private String explanation;
}
