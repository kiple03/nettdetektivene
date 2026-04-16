package org.ntnu.backend.game.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.exception.InvalidOptionFormatException;
import org.ntnu.backend.game.model.MultipleChoiceOption;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptionParser {

  private final ObjectMapper mapper;

  public List<Option> parse(String json, LevelType type) {
    try {
      return switch (type) {
        case MULTIPLE_CHOICE -> {
          List<MultipleChoiceOption> list =
              mapper.readValue(json, new TypeReference<>() {
              });
          yield new ArrayList<>(list);
        }

        case TEXT_QUIZ -> null;
        case PICTURE_QUIZ -> null;
        case MINIGAME -> null;
        case BOSS_BATTLE -> null;
        case TUTORIAL -> null;
      };
    } catch (Exception e) {
      throw new InvalidOptionFormatException("Invalid JSON for " + type);
    }
  }
}

