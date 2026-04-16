package org.ntnu.backend.game.dto;

import java.util.List;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;

public record QuestionResponse(
  int id,
  LevelType levelType,
  String questionText,
  List<Option> options
) {}
