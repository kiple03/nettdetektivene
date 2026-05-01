package org.ntnu.backend.game.dto;

import java.util.List;
import org.ntnu.backend.game.model.PostOfficeQuestion;
import org.ntnu.backend.game.model.enums.LevelType;

/**
 * Response DTO carrying a single quiz question and its answer options.
 *
 * @param id           unique question identifier
 * @param levelType    difficulty / category level of the question
 * @param questionText the question prompt shown to the player
 * @param options      list of selectable answer options
 * @param email        associated e-mail context for post-office questions; {@code null} for other types
 */
public record QuestionResponse(
  int id,
  LevelType levelType,
  String questionText,
  List<OptionResponse> options,
  PostOfficeQuestion.Email email
) {}
