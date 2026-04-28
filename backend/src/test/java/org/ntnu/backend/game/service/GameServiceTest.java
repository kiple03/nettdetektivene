package org.ntnu.backend.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.progress.QuestionAnswer;
import org.ntnu.backend.game.dto.progress.SubmitLevelRequest;
import org.ntnu.backend.game.mapper.GameMapper;
import org.ntnu.backend.game.model.Level;
import org.ntnu.backend.game.model.Question;
import org.ntnu.backend.game.model.Stop;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.repository.StopRepository;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;
import org.ntnu.backend.game.validation.LevelSubmissionValidator;
import org.ntnu.backend.notes.service.NoteService;
import org.ntnu.backend.user.model.enums.RoleEnum;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

  @Mock
  private StopRepository stopRepository;

  @Mock
  private LevelRepository levelRepository;

  @Mock
  private QuestionRepository questionRepository;

  @Mock
  private LevelSubmissionValidator levelSubmissionValidator;

  @Mock
  private ProgressService progressService;

  @Mock
  private NoteService noteService;

  private GameService gameService;

  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();

    OptionValidator optionValidator = new OptionValidator(
            Validation.buildDefaultValidatorFactory().getValidator()
    );
    OptionParser optionParser = new OptionParser(new ObjectMapper(), optionValidator);
    GameMapper gameMapper = new GameMapper(optionParser);

    gameService = new GameService(
            stopRepository,
            levelRepository,
            questionRepository,
            progressService,
            gameMapper,
            levelSubmissionValidator,
            noteService
    );
  }

  // ===== getStopsForUser =====

  @Test
  void getStopsForUser_returnsAllStopsOrdered() {
    Stop stop1 = stop(1, "Nyhetskvartalet", 1);
    Stop stop2 = stop(2, "Fotografen", 2);

    when(stopRepository.findAllByOrderByDisplayOrderAsc())
            .thenReturn(List.of(stop1, stop2));

    when(progressService.isStopAccessible(userId, RoleEnum.STUDENT, stop1))
            .thenReturn(true);
    when(progressService.isStopAccessible(userId, RoleEnum.STUDENT, stop2))
            .thenReturn(true);

    List<StopResponse> result = gameService.getStopsForUser(userId, RoleEnum.STUDENT);

    assertThat(result).hasSize(2);

    assertThat(result.get(0).id()).isEqualTo(1);
    assertThat(result.get(0).name()).isEqualTo("Nyhetskvartalet");
    assertThat(result.get(0).locked()).isFalse();

    assertThat(result.get(1).id()).isEqualTo(2);
    assertThat(result.get(1).name()).isEqualTo("Fotografen");
    assertThat(result.get(1).locked()).isFalse();

    verify(stopRepository).findAllByOrderByDisplayOrderAsc();
    verify(progressService).isStopAccessible(userId, RoleEnum.STUDENT, stop1);
    verify(progressService).isStopAccessible(userId, RoleEnum.STUDENT, stop2);
  }

  @Test
  void getStopsForUser_whenStopIsAccessible_shouldReturnUnlockedStop() {
    Stop stop = stop(1, "Test", 1);

    when(stopRepository.findAllByOrderByDisplayOrderAsc())
            .thenReturn(List.of(stop));
    when(progressService.isStopAccessible(userId, RoleEnum.STUDENT, stop))
            .thenReturn(true);

    List<StopResponse> result = gameService.getStopsForUser(userId, RoleEnum.STUDENT);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().locked()).isFalse();
  }

  @Test
  void getStopsForUser_whenStopIsNotAccessible_shouldReturnLockedStop() {
    Stop stop = stop(1, "Locked stop", 1);

    when(stopRepository.findAllByOrderByDisplayOrderAsc())
            .thenReturn(List.of(stop));
    when(progressService.isStopAccessible(userId, RoleEnum.STUDENT, stop))
            .thenReturn(false);

    List<StopResponse> result = gameService.getStopsForUser(userId, RoleEnum.STUDENT);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().locked()).isTrue();
  }

  @Test
  void getStopsForUser_whenRoleIsTeacher_shouldDelegateRoleToProgressService() {
    Stop stop = stop(1, "Teacher stop", 1);

    when(stopRepository.findAllByOrderByDisplayOrderAsc())
            .thenReturn(List.of(stop));
    when(progressService.isStopAccessible(userId, RoleEnum.TEACHER, stop))
            .thenReturn(true);

    List<StopResponse> result = gameService.getStopsForUser(userId, RoleEnum.TEACHER);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().locked()).isFalse();

    verify(progressService).isStopAccessible(userId, RoleEnum.TEACHER, stop);
  }

  @Test
  void getStopsForUser_emptyDb_returnsEmptyList() {
    when(stopRepository.findAllByOrderByDisplayOrderAsc())
            .thenReturn(List.of());

    List<StopResponse> result = gameService.getStopsForUser(userId, RoleEnum.STUDENT);

    assertThat(result).isEmpty();
    verify(stopRepository).findAllByOrderByDisplayOrderAsc();
  }

  // ===== getLevel =====

  @Test
  void getLevel_foundAndAccessible_returnsUnlockedResponse() {
    Stop stop = stop(1, "Nyhetskvartalet", 1);
    Level level = level(
            1,
            "Ekte eller falskt?",
            "Finn den ekte nyheten",
            1,
            stop,
            LevelType.MULTIPLE_CHOICE
    );

    when(levelRepository.findById(1)).thenReturn(Optional.of(level));
    when(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, level)).thenReturn(true);

    LevelResponse result = gameService.getLevel(1, userId, RoleEnum.STUDENT);

    assertThat(result.id()).isEqualTo(1);
    assertThat(result.title()).isEqualTo("Ekte eller falskt?");
    assertThat(result.description()).isEqualTo("Finn den ekte nyheten");
    assertThat(result.levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
    assertThat(result.order()).isEqualTo(1);
    assertThat(result.locked()).isFalse();

    verify(levelRepository).findById(1);
    verify(progressService).isLevelAccessible(userId, RoleEnum.STUDENT, level);
  }

  @Test
  void getLevel_foundButNotAccessible_returnsLockedResponse() {
    Stop stop = stop(1, "Nyhetskvartalet", 1);
    Level level = level(
            1,
            "Locked level",
            "Locked",
            1,
            stop,
            LevelType.MULTIPLE_CHOICE
    );

    when(levelRepository.findById(1)).thenReturn(Optional.of(level));
    when(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, level)).thenReturn(false);

    LevelResponse result = gameService.getLevel(1, userId, RoleEnum.STUDENT);

    assertThat(result.locked()).isTrue();
  }

  @Test
  void getLevel_notFound_throwsLevelNotFoundException() {
    when(levelRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gameService.getLevel(99, userId, RoleEnum.STUDENT))
            .isInstanceOf(LevelNotFoundException.class);

    verify(levelRepository).findById(99);
    verify(progressService, never()).isLevelAccessible(any(), any(), any());
  }

  // ===== getLevelsForStop =====

  @Test
  void getLevelsForStop_returnsLevelsForStopWithLockState() {
    Stop stop = stop(1, "Nyhetskvartalet", 1);

    Level level1 = level(
            1,
            "Level 1",
            "First",
            1,
            stop,
            LevelType.MULTIPLE_CHOICE
    );

    Level level2 = level(
            2,
            "Level 2",
            "Second",
            2,
            stop,
            LevelType.PICTURE_QUIZ
    );

    when(levelRepository.findByStopIdOrderByOrderAsc(1))
            .thenReturn(List.of(level1, level2));

    when(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, level1))
            .thenReturn(true);
    when(progressService.isLevelAccessible(userId, RoleEnum.STUDENT, level2))
            .thenReturn(false);

    List<LevelResponse> result = gameService.getLevelsForStop(1, userId, RoleEnum.STUDENT);

    assertThat(result).hasSize(2);

    assertThat(result.get(0).id()).isEqualTo(1);
    assertThat(result.get(0).title()).isEqualTo("Level 1");
    assertThat(result.get(0).levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
    assertThat(result.get(0).locked()).isFalse();

    assertThat(result.get(1).id()).isEqualTo(2);
    assertThat(result.get(1).title()).isEqualTo("Level 2");
    assertThat(result.get(1).levelType()).isEqualTo(LevelType.PICTURE_QUIZ);
    assertThat(result.get(1).locked()).isTrue();

    verify(levelRepository).findByStopIdOrderByOrderAsc(1);
    verify(progressService).isLevelAccessible(userId, RoleEnum.STUDENT, level1);
    verify(progressService).isLevelAccessible(userId, RoleEnum.STUDENT, level2);
  }

  @Test
  void getLevelsForStop_noLevels_returnsEmptyList() {
    when(levelRepository.findByStopIdOrderByOrderAsc(99))
            .thenReturn(List.of());

    List<LevelResponse> result = gameService.getLevelsForStop(99, userId, RoleEnum.STUDENT);

    assertThat(result).isEmpty();
    verify(levelRepository).findByStopIdOrderByOrderAsc(99);
  }

  // ===== submitLevel =====

  @Test
  void submitLevel_whenLevelExistsAndNewlyCompleted_shouldValidateRecordProgressAndSaveCompletionTip() {
    int levelId = 1;

    Stop stop = stop(1, "Nyhetskvartalet", 1);
    Level level = level(
            levelId,
            "Level 1",
            "Description",
            1,
            stop,
            LevelType.MULTIPLE_CHOICE
    );

    Question questionOne = question(10, level);
    Question questionTwo = question(20, level);

    SubmitAnswerRequest answerOne = org.mockito.Mockito.mock(SubmitAnswerRequest.class);
    SubmitAnswerRequest answerTwo = org.mockito.Mockito.mock(SubmitAnswerRequest.class);

    QuestionAnswer questionAnswerOne = new QuestionAnswer(10, answerOne);
    QuestionAnswer questionAnswerTwo = new QuestionAnswer(20, answerTwo);

    SubmitLevelRequest request = new SubmitLevelRequest(
            List.of(questionAnswerOne, questionAnswerTwo)
    );

    List<Question> questions = List.of(questionOne, questionTwo);

    when(levelRepository.findById(levelId)).thenReturn(Optional.of(level));
    when(questionRepository.findByLevelIdOrderByIdAsc(levelId)).thenReturn(questions);
    when(progressService.recordCompletionOfLevel(userId, RoleEnum.STUDENT, level)).thenReturn(true);

    gameService.submitLevel(levelId, request, userId, RoleEnum.STUDENT);

    verify(levelRepository).findById(levelId);
    verify(questionRepository).findByLevelIdOrderByIdAsc(levelId);
    verify(levelSubmissionValidator).validate(questions, request.answers());
    verify(progressService).recordCompletionOfLevel(userId, RoleEnum.STUDENT, level);
    verify(noteService).saveCompletionTipIfNeeded(userId, level);
  }

  @Test
  void submitLevel_whenLevelExistsButNotNewlyCompleted_shouldNotSaveCompletionTip() {
    int levelId = 1;

    Stop stop = stop(1, "Nyhetskvartalet", 1);
    Level level = level(
            levelId,
            "Level 1",
            "Description",
            1,
            stop,
            LevelType.MULTIPLE_CHOICE
    );

    Question question = question(10, level);

    SubmitAnswerRequest answer = org.mockito.Mockito.mock(SubmitAnswerRequest.class);
    QuestionAnswer questionAnswer = new QuestionAnswer(10, answer);
    SubmitLevelRequest request = new SubmitLevelRequest(List.of(questionAnswer));

    List<Question> questions = List.of(question);

    when(levelRepository.findById(levelId)).thenReturn(Optional.of(level));
    when(questionRepository.findByLevelIdOrderByIdAsc(levelId)).thenReturn(questions);
    when(progressService.recordCompletionOfLevel(userId, RoleEnum.STUDENT, level)).thenReturn(false);

    gameService.submitLevel(levelId, request, userId, RoleEnum.STUDENT);

    verify(levelRepository).findById(levelId);
    verify(questionRepository).findByLevelIdOrderByIdAsc(levelId);
    verify(levelSubmissionValidator).validate(questions, request.answers());
    verify(progressService).recordCompletionOfLevel(userId, RoleEnum.STUDENT, level);
    verify(noteService, never()).saveCompletionTipIfNeeded(any(UUID.class), any(Level.class));
  }

  @Test
  void submitLevel_whenLevelDoesNotExist_shouldThrowLevelNotFoundException() {
    SubmitLevelRequest request = new SubmitLevelRequest(List.of());

    when(levelRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gameService.submitLevel(99, request, userId, RoleEnum.STUDENT))
            .isInstanceOf(LevelNotFoundException.class);

    verify(levelRepository).findById(99);
    verify(questionRepository, never()).findByLevelIdOrderByIdAsc(any(Integer.class));
    verify(levelSubmissionValidator, never()).validate(any(), any());
    verify(progressService, never()).recordCompletionOfLevel(any(), any(), any());
    verify(noteService, never()).saveCompletionTipIfNeeded(any(), any());
  }

  @Test
  void submitLevel_whenValidatorThrows_shouldNotRecordProgressOrSaveNotes() {
    int levelId = 1;

    Stop stop = stop(1, "Nyhetskvartalet", 1);
    Level level = level(
            levelId,
            "Level 1",
            "Description",
            1,
            stop,
            LevelType.MULTIPLE_CHOICE
    );

    Question question = question(10, level);

    SubmitAnswerRequest answer = org.mockito.Mockito.mock(SubmitAnswerRequest.class);
    QuestionAnswer questionAnswer = new QuestionAnswer(10, answer);
    SubmitLevelRequest request = new SubmitLevelRequest(List.of(questionAnswer));

    List<Question> questions = List.of(question);

    when(levelRepository.findById(levelId)).thenReturn(Optional.of(level));
    when(questionRepository.findByLevelIdOrderByIdAsc(levelId)).thenReturn(questions);

    doThrow(new RuntimeException("Invalid level submission"))
            .when(levelSubmissionValidator)
            .validate(questions, request.answers());

    assertThatThrownBy(() -> gameService.submitLevel(levelId, request, userId, RoleEnum.STUDENT))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid level submission");

    verify(levelRepository).findById(levelId);
    verify(questionRepository).findByLevelIdOrderByIdAsc(levelId);
    verify(levelSubmissionValidator).validate(questions, request.answers());
    verify(progressService, never()).recordCompletionOfLevel(any(), any(), any());
    verify(noteService, never()).saveCompletionTipIfNeeded(any(), any());
  }

  @Test
  void submitLevel_whenTeacherCompletesLevelAndProgressReturnsFalse_shouldNotSaveCompletionTip() {
    int levelId = 1;

    Stop stop = stop(1, "Nyhetskvartalet", 1);
    Level level = level(
            levelId,
            "Teacher level",
            "Description",
            1,
            stop,
            LevelType.MULTIPLE_CHOICE
    );

    Question question = question(10, level);

    SubmitAnswerRequest answer = org.mockito.Mockito.mock(SubmitAnswerRequest.class);
    QuestionAnswer questionAnswer = new QuestionAnswer(10, answer);
    SubmitLevelRequest request = new SubmitLevelRequest(List.of(questionAnswer));

    List<Question> questions = List.of(question);

    when(levelRepository.findById(levelId)).thenReturn(Optional.of(level));
    when(questionRepository.findByLevelIdOrderByIdAsc(levelId)).thenReturn(questions);
    when(progressService.recordCompletionOfLevel(userId, RoleEnum.TEACHER, level)).thenReturn(false);

    gameService.submitLevel(levelId, request, userId, RoleEnum.TEACHER);

    verify(levelSubmissionValidator).validate(questions, request.answers());
    verify(progressService).recordCompletionOfLevel(userId, RoleEnum.TEACHER, level);
    verify(noteService, never()).saveCompletionTipIfNeeded(any(UUID.class), any(Level.class));
  }

  // ===== helpers =====

  private Stop stop(int id, String name, int displayOrder) {
    Stop stop = new Stop();
    stop.setId(id);
    stop.setName(name);
    stop.setDisplayOrder(displayOrder);
    return stop;
  }

  private Level level(
          int id,
          String title,
          String description,
          int order,
          Stop stop,
          LevelType levelType
  ) {
    Level level = new Level();
    level.setId(id);
    level.setTitle(title);
    level.setDescription(description);
    level.setOrder(order);
    level.setStop(stop);
    level.setLevelType(levelType);
    return level;
  }

  private Question question(int id, Level level) {
    Question question = new Question();
    question.setId(id);
    question.setLevel(level);
    question.setQuestionText("Question " + id);
    question.setOptionContent("[]");
    return question;
  }
}