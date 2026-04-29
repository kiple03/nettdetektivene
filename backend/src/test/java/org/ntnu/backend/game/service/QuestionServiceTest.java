package org.ntnu.backend.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.dto.subtypes.SingleChoiceAnswer;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;
import org.ntnu.backend.game.validation.MultipleChoiceAnswerValidator;
import org.ntnu.backend.game.validation.SubmittedAnswerValidatorRegistry;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuestionServiceTest {

  @Mock private QuestionRepository questionRepository;
  @Mock private ProgressService progressService;
  @Mock private LevelRepository levelRepository;

  private QuestionService questionService;


  private Question testQuestion;

  private static final String VALID_OPTIONS_JSON = """
        [
          {"id": 1, "text": "Wrong answer", "imageUrl": "https://example.com/a.jpg", "isCorrect": false, "explanation": "This is wrong"},
          {"id": 2, "text": "Right answer", "imageUrl": "https://example.com/b.jpg", "isCorrect": true, "explanation": "This is correct"},
          {"id": 3, "text": "Also wrong", "imageUrl": "https://example.com/c.jpg", "isCorrect": false, "explanation": "This is also wrong"}
        ]
        """;
  private static final UUID TEST_USER_ID = UUID.randomUUID();
  private static final RoleEnum TEST_ROLE = RoleEnum.STUDENT;

  @BeforeEach
  void setUp() {
    OptionValidator optionValidator = new OptionValidator(
        Validation.buildDefaultValidatorFactory().getValidator());
    OptionParser optionParser = new OptionParser(new ObjectMapper(), optionValidator);
    GameMapper gameMapper = new GameMapper(optionParser);

    SubmittedAnswerValidatorRegistry registry = new SubmittedAnswerValidatorRegistry(
        List.of(new MultipleChoiceAnswerValidator()));

    questionService = new QuestionService(
        questionRepository, gameMapper, optionParser, registry, levelRepository, progressService);
    // Build a test question with real JSON
    Stop stop = new Stop();
    stop.setId(1);

    Level level = new Level();
    level.setId(1);
    level.setLevelType(LevelType.MULTIPLE_CHOICE);
    level.setStop(stop);

    testQuestion = new Question();
    testQuestion.setId(1);
    testQuestion.setQuestionText("Which is correct?");
    testQuestion.setOptionContent(VALID_OPTIONS_JSON);
    testQuestion.setLevel(level);

    when(progressService.isLevelAccessible(any(UUID.class), any(RoleEnum.class), any(Level.class)))
        .thenReturn(true);
  }

  // ===== getQuestionById =====

  @Test
  void getQuestionById_found_returnsStrippedResponse() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    QuestionResponse result = questionService.getQuestionById(1, TEST_USER_ID, TEST_ROLE);

    assertThat(result.id()).isEqualTo(1);
    assertThat(result.questionText()).isEqualTo("Which is correct?");
    assertThat(result.levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
    assertThat(result.options()).hasSize(3);
  }

  @Test
  void getQuestionById_optionsDontLeakAnswers() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    QuestionResponse result = questionService.getQuestionById(1, TEST_USER_ID, TEST_ROLE);

    // Response DTO only has id, text, imageUrl — no isCorrect or explanation
    // This is enforced by the type system, but we verify the response
    // doesn't contain answer data by checking the JSON output
    String json = result.options().toString();
    assertThat(json).doesNotContain("isCorrect");
    assertThat(json).doesNotContain("explanation");
  }

  @Test
  void getQuestionById_notFound_throws() {
    when(questionRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> questionService.getQuestionById(99, TEST_USER_ID, TEST_ROLE))
        .isInstanceOf(QuestionNotFoundException.class);
  }

  // ===== getQuestionsForLevel =====

  @Test
  void getQuestionsForLevel_returnsAll() {
    Question q2 = new Question();
    q2.setId(2);
    q2.setQuestionText("Second question");
    q2.setOptionContent(VALID_OPTIONS_JSON);
    q2.setLevel(testQuestion.getLevel());

    when(levelRepository.findById(1)).thenReturn(Optional.of(testQuestion.getLevel()));
    when(questionRepository.findByLevelIdOrderByIdAsc(1))
        .thenReturn(List.of(testQuestion, q2));

    List<QuestionResponse> result = questionService.getQuestionsForLevel(1, TEST_USER_ID, TEST_ROLE);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).questionText()).isEqualTo("Which is correct?");
    assertThat(result.get(1).questionText()).isEqualTo("Second question");
  }

  @Test
  void getQuestionsForLevel_noQuestions_returnsEmpty() {
    Level emptyLevel = new Level();
    emptyLevel.setId(99);
    emptyLevel.setStop(testQuestion.getLevel().getStop());

    when(levelRepository.findById(99)).thenReturn(Optional.of(emptyLevel));
    when(questionRepository.findByLevelIdOrderByIdAsc(99))
        .thenReturn(List.of());

    List<QuestionResponse> result = questionService.getQuestionsForLevel(99, TEST_USER_ID, TEST_ROLE);

    assertThat(result).isEmpty();
  }

  // ===== submitAnswer =====

  @Test
  void submitAnswer_correctAnswer_returnsTrue() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 2);

    SubmitAnswerResponse result = questionService.submitAnswer(1, answer, TEST_USER_ID, TEST_ROLE);

    assertThat(result.correct()).isTrue();
    assertThat(result.feedback()).isEqualTo("This is correct");
  }

  @Test
  void submitAnswer_wrongAnswer_returnsFalse() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 1);

    SubmitAnswerResponse result = questionService.submitAnswer(1, answer, TEST_USER_ID, TEST_ROLE);

    assertThat(result.correct()).isFalse();
    assertThat(result.feedback()).isEqualTo("This is wrong");
  }

  @Test
  void submitAnswer_anotherWrongAnswer_returnsFalseWithCorrectFeedback() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 3);

    SubmitAnswerResponse result = questionService.submitAnswer(1, answer, TEST_USER_ID, TEST_ROLE);

    assertThat(result.correct()).isFalse();
    assertThat(result.feedback()).isEqualTo("This is also wrong");
  }

  @Test
  void submitAnswer_nonexistentOption_throws() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 99);

    assertThatThrownBy(() -> questionService.submitAnswer(1, answer, TEST_USER_ID, TEST_ROLE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void submitAnswer_questionNotFound_throws() {
    when(questionRepository.findById(99)).thenReturn(Optional.empty());

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 1);

    assertThatThrownBy(() -> questionService.submitAnswer(99, answer, TEST_USER_ID, TEST_ROLE))
        .isInstanceOf(QuestionNotFoundException.class);
  }
}