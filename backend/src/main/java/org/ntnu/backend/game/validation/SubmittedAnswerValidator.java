package org.ntnu.backend.game.validation;

import java.util.List;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.model.Question;

/**
 * Strategy for validating a student's submitted answer against a {@link Question}'s
 * stored option content.
 *
 * <p>One implementation per {@link LevelType}. Implementations know the JSON
 * shape of their level type, the expected {@link SubmitAnswerRequest} subtype,
 * and how to compare the two to produce a result.
 */
public interface SubmittedAnswerValidator {

  /**
   * Returns the level type this validator handles. Used by the registry to
   * dispatch requests to the correct implementation.
   */
  LevelType supportedType();

  /**
   * Compares the submitted answer against the correct answer stored in the
   * option content JSON.
   *
   * @param options list of the question responseUrls from {@code Question.optionContent}
   * @param submission the student's submission
   * @return the validation result, including feedback for the student
   * @throws IllegalArgumentException if the submission shape doesn't match this level type
   */
  ValidationResult validate(List<Option> options, SubmitAnswerRequest submission);}