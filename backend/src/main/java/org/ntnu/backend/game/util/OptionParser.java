package org.ntnu.backend.game.util;

import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.PictureQuizOption;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

import org.ntnu.backend.exception.InvalidOptionFormatException;
import org.ntnu.backend.exception.OptionValidationException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Deserializes question option JSON into typed {@link Option} subclasses
 * based on the question's {@link LevelType}.
 *
 * <p>After parsing, the resulting options are validated by
 * {@link OptionValidator} to catch malformed data early, both
 * field-level constraints and business rules are checked.
 *
 * <p>Adding support for a new level type requires a new case in the
 * switch and a corresponding {@link Option} subclass (which is sealed).
 *
 * @see OptionValidator
 * @see Option
 */
@Component
@RequiredArgsConstructor
public class OptionParser {

  private final ObjectMapper mapper;
  private final OptionValidator validator;

  /**
   * Parses the JSON string into a list of typed options and validates them.
   *
   * @param json the raw JSON from {@code Question.optionContent}
   * @param type the level type, determines which concrete class to deserialize into
   *
   * @return validated list of typed options
   * @throws InvalidOptionFormatException if the JSON cannot be parsed
   * @throws OptionValidationException if parsed options fail validation
   */
  public List<Option> parse(String json, LevelType type) {
    List<Option> options;
    try {
      options = switch (type) {
        case MULTIPLE_CHOICE -> {
          List<MultipleChoiceOption> list =
                  mapper.readValue(json, new TypeReference<>() {});
          yield new ArrayList<>(list);
        }
        case PICTURE_QUIZ -> {
          List<PictureQuizOption> list =
                  mapper.readValue(json, new TypeReference<>() {});
          yield new ArrayList<>(list);
        }
        case PASSWORD_STRENGTH -> {
          List<PasswordStrengthOption> list = mapper.readValue(json, new TypeReference<>() {});
          yield new ArrayList<>(list);
        }
        case TEXT_QUIZ -> null;
        case MINIGAME -> null;
        case BOSS_BATTLE -> null;
        case TUTORIAL   -> null;
        case POST_OFFICE -> null;
        case CLASSIFY_ITEMS -> null;
      };
    } catch (JacksonException e) {
      throw new InvalidOptionFormatException("Invalid JSON for " + type + ": " + e.getMessage());
    }

    validator.validate(options, type);
    return options;
  }
}