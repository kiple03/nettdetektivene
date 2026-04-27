package org.ntnu.backend.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ntnu.backend.game.dto.PostOfficeOptionResponse;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;

/**
 * A single flaggable element in a {@link LevelType#POST_OFFICE} question. Stored as JSON
 * inside {@link Question#getOptionContent()} and deserialized by {@link OptionParser}.
 *
 * <p>At least one option per question must have {@code isSuspicious = true},
 * enforced by {@link OptionValidator}.
 *
 * <p>The {@code isSuspicious} and {@code explanation} fields are server-only —
 * they are used by validators but never included in API responses.
 * The corresponding response DTO ({@link PostOfficeOptionResponse})
 * only exposes {@code id} and {@code type}.
 *
 * @see Option
 * @see PostOfficeOptionResponse
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class PostOfficeOption implements Option {

  @NotBlank
  private String id;

  @NotBlank
  private String type;

  @JsonProperty("isSuspicious")
  private boolean isSuspicious;

  @NotBlank
  private String explanation;
}