package org.ntnu.backend.game.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.ntnu.backend.exception.InvalidLevelSubmissionException;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.progress.QuestionAnswer;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.util.OptionParser;
import org.springframework.stereotype.Component;

/**
 * Validates a batch of answers submitted for a complete level.
 *
 * <p>A valid batch must contain exactly one answer for each question in the
 * level and every answer must be correct. Any deviation results in an
 * {@link InvalidLevelSubmissionException} which maps to a 400 response.
 *
 * <p>This validator is the commit point for level submissions.
 * It is separate from the per-question preview flow in
 * {@code QuestionService.submitAnswer}, which provides immediate feedback
 * during play but does not record progress.
 *
 * @see org.ntnu.backend.game.service.GameService#submitLevel
 */
@Component
@AllArgsConstructor
public class LevelSubmissionValidator {

  private final OptionParser optionParser;
  private final SubmittedAnswerValidatorRegistry validatorRegistry;

  /**
   * Validates that the submitted answers cover exactly the level's questions
   * and that every answer is correct.
   *
   * @param questions the questions belonging to the level being submitted
   * @param answers the answers submitted by the client
   * @throws InvalidLevelSubmissionException if the batch shape is wrong or
   *                                         any answer is incorrect
   */
  public void validate(List<Question> questions, List<QuestionAnswer> answers) {
    validateShape(questions, answers);
    validateAllCorrect(questions, answers);
  }

  private void validateShape(List<Question> questions, List<QuestionAnswer> answers) {
    if (questions.size() != answers.size()) {
      throw new InvalidLevelSubmissionException(
          "Expected " + questions.size() + " answers, got " + answers.size());
    }

    Set<Integer> expectedIds = questions.stream()
        .map(Question::getId)
        .collect(Collectors.toSet());
    Set<Integer> submittedIds = answers.stream()
        .map(QuestionAnswer::questionId)
        .collect(Collectors.toSet());

    if (!expectedIds.equals(submittedIds)) {
      throw new InvalidLevelSubmissionException(
          "Submitted question IDs do not match the level's questions");
    }
  }

  private void validateAllCorrect(List<Question> questions, List<QuestionAnswer> answers) {
    Map<Integer, SubmitAnswerRequest> answerByQuestionId = answers.stream()
        .collect(Collectors.toMap(QuestionAnswer::questionId, QuestionAnswer::answer));

    for (Question question : questions) {
      SubmitAnswerRequest answer = answerByQuestionId.get(question.getId());
      LevelType type = question.getLevel().getLevelType();
      List<Option> options = optionParser.parse(question.getOptionContent(), type);
      SubmittedAnswerValidator validator = validatorRegistry.forType(type);

      if (!validator.validate(options, answer).correct()) {
        throw new InvalidLevelSubmissionException(
            "Answer to question " + question.getId() + " is incorrect");
      }
    }
  }
}