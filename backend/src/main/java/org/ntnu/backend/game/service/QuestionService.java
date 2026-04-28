package org.ntnu.backend.game.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.QuestionResponse;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.validation.SubmittedAnswerValidator;
import org.ntnu.backend.game.validation.ValidationResult;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.ntnu.backend.game.validation.SubmittedAnswerValidatorRegistry;

/**
 * Handles the question read and answer submission flow.
 *
 * <p>For reads, delegates to {@link GameMapper} which parses the JSON
 * options via {@link OptionParser} and strips answer data from the response.
 *
 * <p>For submissions, parses the stored options, looks up the correct
 * {@link SubmittedAnswerValidator} via the {@link SubmittedAnswerValidatorRegistry} for
 * the question's level type, and returns feedback to the student.
 *
 * @see GameMapper
 * @see SubmittedAnswerValidatorRegistry
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final GameMapper gameMapper;
  private final OptionParser optionParser;
  private final SubmittedAnswerValidatorRegistry validatorRegistry;


  /**
   * Fetches a question by ID and maps it to a response with answer
   * data excluded. Options are parsed from JSON and converted to
   * their corresponding response DTOs.
   *
   * @param id the question ID
   * @return the question with valid options for the client
   * @throws QuestionNotFoundException if no question exists with this ID
   */
  public QuestionResponse getQuestionById(int id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new QuestionNotFoundException(id));
    return gameMapper.toQuestionResponse(question);
  }

  /**
   * Validates a student's submitted answer against the correct answer
   * stored in the question's option content.
   *
   * <p>The flow:
   * <ol>
   *   <li>Find the question</li>
   *   <li>Parse options from JSON via {@link OptionParser}</li>
   *   <li>Look up the validator for this level type</li>
   *   <li>Compare submission against correct answer</li>
   *   <li>Return result with feedback</li>
   * </ol>
   *
   * @param questionId the question being answered
   * @param request the student's submission (polymorphic by level type)
   * @return whether the answer is correct, with an explanation
   * @throws QuestionNotFoundException if no question exists with this ID
   * @throws IllegalArgumentException if the submission shape doesn't match
   *         the level type, or the selected option ID doesn't exist.
   */
  public SubmitAnswerResponse submitAnswer(int questionId, SubmitAnswerRequest request) {
    Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new QuestionNotFoundException(questionId));

    LevelType type = question.getLevel().getLevelType();
    List<Option> options = optionParser.parse(question.getOptionContent(), type);

    SubmittedAnswerValidator validator = validatorRegistry.forType(type);
    ValidationResult result = validator.validate(options, request);

    return new SubmitAnswerResponse(
            result.correct(),
            result.feedback(),
            result.correctOptionIds(),
            result.wrongSelectedOptionIds()
    );
  }

  public List<QuestionResponse> getQuestionsForLevel(int levelId, JwtAuthenticatedPrincipal principal) {
    UUID classroomId = principal.getClassroomId();
    List<Question> questions = (principal.isStudent() && classroomId != null)
        ? questionRepository.findByLevelIdForClassroom(levelId, classroomId)
        : questionRepository.findByLevelIdAndClassroomIdIsNull(levelId);
    return questions.stream()
        .map(gameMapper::toQuestionResponse)
        .toList();
  }
}
