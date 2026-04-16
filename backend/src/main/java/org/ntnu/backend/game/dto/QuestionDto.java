package org.ntnu.backend.game.dto;

import java.util.List;
import org.ntnu.backend.game.model.Option;

public record QuestionDto (
  int id,
  String questionText,
  List<Option> options
) {}
