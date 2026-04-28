package org.ntnu.backend.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.dto.subtypes.SingleChoiceAnswer;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;
import org.ntnu.backend.game.validation.MultipleChoiceAnswerValidator;
import org.ntnu.backend.game.validation.SubmittedAnswerValidatorRegistry;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

  @Mock private QuestionRepository questionRepository;

  private QuestionService questionService;

  private Question testQuestion;

  private static final String VALID_OPTIONS_JSON = """
        [
          {"id": 1, "text": "Wrong answer", "imageUrl": "/images/1", "isCorrect": false, "explanation": "This is wrong"},
          {"id": 2, "text": "Right answer", "imageUrl": "/images/2", "isCorrect": true, "explanation": "This is correct"},
          {"id": 3, "text": "Also wrong", "imageUrl": "/images/3", "isCorrect": false, "explanation": "This is also wrong"}
        ]
        """;

  @BeforeEach
  void setUp() {
    // Build real dependency chain: validator -> parser -> mapper
    OptionValidator optionValidator = new OptionValidator(
        Validation.buildDefaultValidatorFactory().getValidator());
    OptionParser optionParser = new OptionParser(new ObjectMapper(), optionValidator);
    GameMapper gameMapper = new GameMapper(optionParser);

    // Build real validator registry with the real validator
    SubmittedAnswerValidatorRegistry registry = new SubmittedAnswerValidatorRegistry(
        List.of(new MultipleChoiceAnswerValidator()));

    questionService = new QuestionService(
        questionRepository, gameMapper, optionParser, registry);

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
  }

  // ===== getQuestionById =====

  @Test
  void getQuestionById_found_returnsStrippedResponse() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    QuestionResponse result = questionService.getQuestionById(1);

    assertThat(result.id()).isEqualTo(1);
    assertThat(result.questionText()).isEqualTo("Which is correct?");
    assertThat(result.levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
    assertThat(result.options()).hasSize(3);
  }

  @Test
  void getQuestionById_optionsDontLeakAnswers() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    QuestionResponse result = questionService.getQuestionById(1);

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

    assertThatThrownBy(() -> questionService.getQuestionById(99))
        .isInstanceOf(QuestionNotFoundException.class);
  }

  // ===== getQuestionsForLevel =====

  private JwtAuthenticatedPrincipal teacherPrincipal() {
    Collection<SimpleGrantedAuthority> auth = List.of(new SimpleGrantedAuthority("ROLE_TEACHER"));
    return new JwtAuthenticatedPrincipal("teacher@test.no", UUID.randomUUID(), RoleEnum.TEACHER, null, auth);
  }

  private JwtAuthenticatedPrincipal studentPrincipal(UUID classroomId) {
    Collection<SimpleGrantedAuthority> auth = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    return new JwtAuthenticatedPrincipal("student@test.no", UUID.randomUUID(), RoleEnum.STUDENT, classroomId, auth);
  }

  @Test
  void getQuestionsForLevel_teacher_returnsDefaultsOnly() {
    Question q2 = new Question();
    q2.setId(2);
    q2.setQuestionText("Second question");
    q2.setOptionContent(VALID_OPTIONS_JSON);
    q2.setLevel(testQuestion.getLevel());

    when(questionRepository.findByLevelIdAndClassroomIdIsNull(1))
        .thenReturn(List.of(testQuestion, q2));

    List<QuestionResponse> result = questionService.getQuestionsForLevel(1, teacherPrincipal());

    assertThat(result).hasSize(2);
    assertThat(result.get(0).questionText()).isEqualTo("Which is correct?");
    assertThat(result.get(1).questionText()).isEqualTo("Second question");
  }

  @Test
  void getQuestionsForLevel_teacher_noQuestions_returnsEmpty() {
    when(questionRepository.findByLevelIdAndClassroomIdIsNull(99))
        .thenReturn(List.of());

    List<QuestionResponse> result = questionService.getQuestionsForLevel(99, teacherPrincipal());

    assertThat(result).isEmpty();
  }

  @Test
  void getQuestionsForLevel_student_mergesDefaultAndClassroomQuestions() {
    UUID classroomId = UUID.randomUUID();

    Question classroomQ = new Question();
    classroomQ.setId(2);
    classroomQ.setQuestionText("Teacher-added question");
    classroomQ.setOptionContent(VALID_OPTIONS_JSON);
    classroomQ.setLevel(testQuestion.getLevel());
    classroomQ.setClassroomId(classroomId);

    when(questionRepository.findByLevelIdForClassroom(1, classroomId))
        .thenReturn(List.of(testQuestion, classroomQ));

    List<QuestionResponse> result = questionService.getQuestionsForLevel(1, studentPrincipal(classroomId));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).questionText()).isEqualTo("Which is correct?");
    assertThat(result.get(1).questionText()).isEqualTo("Teacher-added question");
  }

  @Test
  void getQuestionsForLevel_student_noClassroom_returnsDefaultsOnly() {
    when(questionRepository.findByLevelIdAndClassroomIdIsNull(1))
        .thenReturn(List.of(testQuestion));

    List<QuestionResponse> result = questionService.getQuestionsForLevel(1, studentPrincipal(null));

    assertThat(result).hasSize(1);
  }

  // ===== submitAnswer =====

  @Test
  void submitAnswer_correctAnswer_returnsTrue() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 2);

    SubmitAnswerResponse result = questionService.submitAnswer(1, answer);

    assertThat(result.correct()).isTrue();
    assertThat(result.feedback()).isEqualTo("This is correct");
  }

  @Test
  void submitAnswer_wrongAnswer_returnsFalse() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 1);

    SubmitAnswerResponse result = questionService.submitAnswer(1, answer);

    assertThat(result.correct()).isFalse();
    assertThat(result.feedback()).isEqualTo("This is wrong");
  }

  @Test
  void submitAnswer_anotherWrongAnswer_returnsFalseWithCorrectFeedback() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 3);

    SubmitAnswerResponse result = questionService.submitAnswer(1, answer);

    assertThat(result.correct()).isFalse();
    assertThat(result.feedback()).isEqualTo("This is also wrong");
  }

  @Test
  void submitAnswer_nonexistentOption_throws() {
    when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 99);

    assertThatThrownBy(() -> questionService.submitAnswer(1, answer))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void submitAnswer_questionNotFound_throws() {
    when(questionRepository.findById(99)).thenReturn(Optional.empty());

    SingleChoiceAnswer answer = new SingleChoiceAnswer();
    ReflectionTestUtils.setField(answer, "selectedId", 1);

    assertThatThrownBy(() -> questionService.submitAnswer(99, answer))
        .isInstanceOf(QuestionNotFoundException.class);
  }
}