package org.ntnu.backend.classroom.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.classroom.dto.createquestion.*;
import org.ntnu.backend.exception.LevelNotFoundException;
import org.ntnu.backend.exception.QuestionNotFoundException;
import org.ntnu.backend.game.dto.teacher.*;
import org.ntnu.backend.game.model.*;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.repository.LevelRepository;
import org.ntnu.backend.game.repository.QuestionRepository;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.util.OptionParser;
import org.ntnu.backend.game.util.OptionValidator;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TeacherQuestionServiceTest {

  @Mock private QuestionRepository questionRepository;
  @Mock private LevelRepository levelRepository;
  @Mock private ClassroomAuthorizationService classroomAuthorizationService;
  @Mock private OptionParser optionParser;
  @Mock private OptionValidator optionValidator;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks
  private TeacherQuestionService service;

  private UUID classroomId;
  private JwtAuthenticatedPrincipal principal;

  @BeforeEach
  void setUp() {
    classroomId = UUID.randomUUID();
    principal = mock(JwtAuthenticatedPrincipal.class);
  }

  private Level makeLevel(int id, LevelType type) {
    Level level = new Level();
    level.setId(id);
    level.setLevelType(type);
    return level;
  }

  private Question makeQuestion(int id, Level level, boolean isDefault, UUID ownerClassroom) {
    Question q = new Question();
    q.setId(id);
    q.setQuestionText("Question " + id);
    q.setOptionContent("[{}]");
    q.setLevel(level);
    q.setDefault(isDefault);
    q.setClassroomId(ownerClassroom);
    return q;
  }

  /** Stubs save to echo back with an ID, and serialization to return dummy JSON. */
  private void stubSaveAndSerialize() throws Exception {
    when(objectMapper.writeValueAsString(any())).thenReturn("[{}]");
    when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
      Question q = inv.getArgument(0);
      if (q.getId() == 0) q.setId(99);
      return q;
    });
  }

  private CreateMultipleChoiceQuestionRequest mcCreateReq() {
    return new CreateMultipleChoiceQuestionRequest("MC question?", List.of(
        new CreateMultipleChoiceOptionRequest("Right", null, null, true, "Correct!"),
        new CreateMultipleChoiceOptionRequest("Wrong", null, null, false, "Nope")
    ));
  }

  private UpdateMultipleChoiceQuestionRequest mcUpdateReq() {
    return new UpdateMultipleChoiceQuestionRequest("MC updated?", List.of(
        new CreateMultipleChoiceOptionRequest("Still right", null, null, true, "Yes"),
        new CreateMultipleChoiceOptionRequest("Still wrong", null, null, false, "No")
    ));
  }

  private CreatePictureQuizQuestionRequest pqCreateReq() {
    return new CreatePictureQuizQuestionRequest("PQ question?", List.of(
        new CreatePictureQuizOptionRequest("Real", "http://img/real.png", true, "It's real"),
        new CreatePictureQuizOptionRequest("Fake", "http://img/fake.png", false, "AI generated")
    ));
  }

  private UpdatePictureQuizQuestionRequest pqUpdateReq() {
    return new UpdatePictureQuizQuestionRequest("PQ updated?", List.of(
        new CreatePictureQuizOptionRequest("Real updated", "http://img/real2.png", true, "Still real"),
        new CreatePictureQuizOptionRequest("Fake updated", "http://img/fake2.png", false, "Still fake")
    ));
  }

  private CreateClassifyItemsQuestionRequest ciCreateReq() {
    return new CreateClassifyItemsQuestionRequest("CI question?",
        List.of("Safe", "Dangerous"),
        List.of(
            new CreateClassifyItemsItemRequest("HTTPS site", "Safe", "Encrypted"),
            new CreateClassifyItemsItemRequest("HTTP site", "Dangerous", "Not encrypted")
        ));
  }

  private UpdateClassifyItemsQuestionRequest ciUpdateReq() {
    return new UpdateClassifyItemsQuestionRequest("CI updated?",
        List.of("Good", "Bad"),
        List.of(
            new CreateClassifyItemsItemRequest("Strong password", "Good", "Nice"),
            new CreateClassifyItemsItemRequest("Weak password", "Bad", "Too short")
        ));
  }

  private CreatePasswordStrengthQuestionRequest psCreateReq() {
    return new CreatePasswordStrengthQuestionRequest(
        "PS question?", 8, true, true, true, false, "Strong!", "Too weak!");
  }

  private UpdatePasswordStrengthQuestionRequest psUpdateReq() {
    return new UpdatePasswordStrengthQuestionRequest(
        "PS updated?", 12, true, true, true, true, "Great!", "Try harder!");
  }

  private CreateTextQuizQuestionRequest tqCreateReq() {
    return new CreateTextQuizQuestionRequest("TQ question?", List.of(
        new CreateTextQuizOptionRequest("Correct answer", true, "Right!"),
        new CreateTextQuizOptionRequest("Wrong answer", false, "Nope")
    ));
  }

  private UpdateTextQuizQuestionRequest tqUpdateReq() {
    return new UpdateTextQuizQuestionRequest("TQ updated?", List.of(
        new CreateTextQuizOptionRequest("Updated correct", true, "Still right"),
        new CreateTextQuizOptionRequest("Updated wrong", false, "Still wrong")
    ));
  }

  private List<Option> mcOptions() {
    return List.of(
        new MultipleChoiceOption(1, "Right", null, null, true, "Correct!"),
        new MultipleChoiceOption(2, "Wrong", null, null, false, "Nope")
    );
  }

  private List<Option> pqOptions() {
    return List.of(
        new PictureQuizOption(1, "Real", "http://img/real.png", true, "It's real"),
        new PictureQuizOption(2, "Fake", "http://img/fake.png", false, "AI generated")
    );
  }

  private List<Option> ciOptions() {
    return List.of(
        new ClassifyItemsOption("item-1", "HTTPS site", List.of("Safe", "Dangerous"), "Safe", "Encrypted"),
        new ClassifyItemsOption("item-2", "HTTP site", List.of("Safe", "Dangerous"), "Dangerous", "Not encrypted")
    );
  }

  private List<Option> psOptions() {
    return List.of(
        new PasswordStrengthOption(1, 8, true, true, true, false, null, "Strong!", "Too weak!")
    );
  }

  /** TextQuiz reuses MultipleChoiceOption with null imageUrl */
  private List<Option> tqOptions() {
    return List.of(
        new MultipleChoiceOption(1, "Correct answer", null, null, true, "Right!"),
        new MultipleChoiceOption(2, "Wrong answer", null, null, false, "Nope")
    );
  }

  @Nested
  class ListQuestions {

    @Test
    void returnsAllQuestionsForLevel() {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      when(questionRepository.findByLevelIdForClassroom(1, classroomId))
          .thenReturn(List.of(
              makeQuestion(1, level, true, null),
              makeQuestion(2, level, false, classroomId)
          ));
      when(optionParser.parse(any(), eq(LevelType.MULTIPLE_CHOICE))).thenReturn(mcOptions());

      List<TeacherQuestionResponse> result =
          service.listQuestionsForTeacher(classroomId, 1, principal);

      assertThat(result).hasSize(2);
      assertThat(result.getFirst().isDefault()).isTrue();
      assertThat(result.get(1).isDefault()).isFalse();
      verify(classroomAuthorizationService).verifyAccess(principal, classroomId);
    }

    @Test
    void verifiesAccessBeforeQuerying() {
      doThrow(new AccessDeniedException("denied"))
          .when(classroomAuthorizationService).verifyAccess(principal, classroomId);

      assertThatThrownBy(() ->
          service.listQuestionsForTeacher(classroomId, 1, principal)
      ).isInstanceOf(AccessDeniedException.class);

      verifyNoInteractions(questionRepository);
    }

    @Test
    void returnsEmptyListWhenNoQuestions() {
      when(questionRepository.findByLevelIdForClassroom(1, classroomId))
          .thenReturn(List.of());

      List<TeacherQuestionResponse> result =
          service.listQuestionsForTeacher(classroomId, 1, principal);

      assertThat(result).isEmpty();
    }
  }


  @Nested
  class CreateQuestion {

    @Test
    void createsMultipleChoiceQuestion() throws Exception {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      when(levelRepository.findById(1)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.MULTIPLE_CHOICE))).thenReturn(mcOptions());

      TeacherQuestionResponse resp =
          service.createQuestion(classroomId, 1, mcCreateReq(), principal);

      assertThat(resp.questionText()).isEqualTo("MC question?");
      assertThat(resp.levelType()).isEqualTo(LevelType.MULTIPLE_CHOICE);
      assertThat(resp.isDefault()).isFalse();
      assertThat(resp.classroomId()).isEqualTo(classroomId);
      assertThat(resp.options()).hasSize(2);
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherMultipleChoiceOptionResponse.class);

      verify(optionValidator).validate(anyList(), eq(LevelType.MULTIPLE_CHOICE));

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), eq(LevelType.MULTIPLE_CHOICE));
      List<Option> built = captor.getValue();
      assertThat(built).hasSize(2);
      assertThat(built.getFirst()).isInstanceOf(MultipleChoiceOption.class);
      MultipleChoiceOption mc = (MultipleChoiceOption) built.getFirst();
      assertThat(mc.getId()).isEqualTo(1);
      assertThat(mc.getText()).isEqualTo("Right");
      assertThat(mc.isCorrect()).isTrue();
    }

    @Test
    void createsPictureQuizQuestion() throws Exception {
      Level level = makeLevel(2, LevelType.PICTURE_QUIZ);
      when(levelRepository.findById(2)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.PICTURE_QUIZ))).thenReturn(pqOptions());

      TeacherQuestionResponse resp =
          service.createQuestion(classroomId, 2, pqCreateReq(), principal);

      assertThat(resp.levelType()).isEqualTo(LevelType.PICTURE_QUIZ);
      assertThat(resp.options()).hasSize(2);
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherPictureQuizOptionResponse.class);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), eq(LevelType.PICTURE_QUIZ));
      List<Option> built = captor.getValue();
      assertThat(built).hasSize(2);
      PictureQuizOption pq = (PictureQuizOption) built.getFirst();
      assertThat(pq.text()).isEqualTo("Real");
      assertThat(pq.imageUrl()).isEqualTo("http://img/real.png");
      assertThat(pq.isCorrect()).isTrue();
    }

    @Test
    void createsClassifyItemsQuestion() throws Exception {
      Level level = makeLevel(3, LevelType.CLASSIFY_ITEMS);
      when(levelRepository.findById(3)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.CLASSIFY_ITEMS))).thenReturn(ciOptions());

      TeacherQuestionResponse resp =
          service.createQuestion(classroomId, 3, ciCreateReq(), principal);

      assertThat(resp.levelType()).isEqualTo(LevelType.CLASSIFY_ITEMS);
      assertThat(resp.options()).hasSize(2);
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherClassifyItemsOptionResponse.class);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), eq(LevelType.CLASSIFY_ITEMS));
      List<Option> built = captor.getValue();
      assertThat(built).hasSize(2);
      ClassifyItemsOption ci = (ClassifyItemsOption) built.getFirst();
      assertThat(ci.getKey()).isEqualTo("item-1");
      assertThat(ci.getText()).isEqualTo("HTTPS site");
      assertThat(ci.getLabels()).containsExactly("Safe", "Dangerous");
      assertThat(ci.getCorrectLabel()).isEqualTo("Safe");
    }

    @Test
    void createsPasswordStrengthQuestion() throws Exception {
      Level level = makeLevel(4, LevelType.PASSWORD_STRENGTH);
      when(levelRepository.findById(4)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.PASSWORD_STRENGTH))).thenReturn(psOptions());

      TeacherQuestionResponse resp =
          service.createQuestion(classroomId, 4, psCreateReq(), principal);

      assertThat(resp.levelType()).isEqualTo(LevelType.PASSWORD_STRENGTH);
      assertThat(resp.options()).hasSize(1);
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherPasswordStrengthOptionResponse.class);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), eq(LevelType.PASSWORD_STRENGTH));
      List<Option> built = captor.getValue();
      assertThat(built).hasSize(1);
      PasswordStrengthOption ps = (PasswordStrengthOption) built.getFirst();
      assertThat(ps.getMinLength()).isEqualTo(8);
      assertThat(ps.isRequireDigit()).isTrue();
      assertThat(ps.isRequireUpper()).isTrue();
      assertThat(ps.isRequireLower()).isTrue();
      assertThat(ps.isRequireSymbol()).isFalse();
      assertThat(ps.getSuccessFeedback()).isEqualTo("Strong!");
      assertThat(ps.getFailureFeedback()).isEqualTo("Too weak!");
    }

    @Test
    void createsTextQuizQuestion() throws Exception {
      Level level = makeLevel(5, LevelType.TEXT_QUIZ);
      when(levelRepository.findById(5)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.TEXT_QUIZ))).thenReturn(tqOptions());

      TeacherQuestionResponse resp =
          service.createQuestion(classroomId, 5, tqCreateReq(), principal);

      assertThat(resp.levelType()).isEqualTo(LevelType.TEXT_QUIZ);
      assertThat(resp.options()).hasSize(2);
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherTextQuizOptionResponse.class);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), eq(LevelType.TEXT_QUIZ));
      List<Option> built = captor.getValue();
      assertThat(built).hasSize(2);
      MultipleChoiceOption tq = (MultipleChoiceOption) built.getFirst();
      assertThat(tq.getText()).isEqualTo("Correct answer");
      assertThat(tq.getImageUrl()).isNull();
      assertThat(tq.isCorrect()).isTrue();
    }

    @Test
    void throwsWhenLevelNotFound() {
      when(levelRepository.findById(999)).thenReturn(Optional.empty());

      assertThatThrownBy(() ->
          service.createQuestion(classroomId, 999, mcCreateReq(), principal)
      ).isInstanceOf(LevelNotFoundException.class);

      verifyNoInteractions(questionRepository);
    }

    @Test
    void throwsForUnsupportedLevelType() {
      Level unsupported = makeLevel(10, LevelType.POST_OFFICE);
      when(levelRepository.findById(10)).thenReturn(Optional.of(unsupported));

      assertThatThrownBy(() ->
          service.createQuestion(classroomId, 10, mcCreateReq(), principal)
      ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("does not support teacher-added questions");
    }

    @Test
    void setsQuestionAsNonDefaultWithClassroomId() throws Exception {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      when(levelRepository.findById(1)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), any())).thenReturn(mcOptions());

      service.createQuestion(classroomId, 1, mcCreateReq(), principal);

      ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
      verify(questionRepository).save(captor.capture());
      Question saved = captor.getValue();
      assertThat(saved.isDefault()).isFalse();
      assertThat(saved.getClassroomId()).isEqualTo(classroomId);
      assertThat(saved.getLevel()).isEqualTo(level);
    }

    @Test
    void throwsWhenSerializationFails() throws Exception {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      when(levelRepository.findById(1)).thenReturn(Optional.of(level));
      when(objectMapper.writeValueAsString(any()))
          .thenThrow(new RuntimeException("serialization error"));

      assertThatThrownBy(() ->
          service.createQuestion(classroomId, 1, mcCreateReq(), principal)
      ).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Failed to serialize");
    }

    @Test
    void mcOptionIdsAreOneBased() throws Exception {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      when(levelRepository.findById(1)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), any())).thenReturn(mcOptions());

      service.createQuestion(classroomId, 1, mcCreateReq(), principal);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), any());
      List<Option> built = captor.getValue();
      assertThat(((MultipleChoiceOption) built.getFirst()).getId()).isEqualTo(1);
      assertThat(((MultipleChoiceOption) built.get(1)).getId()).isEqualTo(2);
    }

    @Test
    void ciItemKeysAreSequential() throws Exception {
      Level level = makeLevel(3, LevelType.CLASSIFY_ITEMS);
      when(levelRepository.findById(3)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), any())).thenReturn(ciOptions());

      service.createQuestion(classroomId, 3, ciCreateReq(), principal);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), any());
      List<Option> built = captor.getValue();
      assertThat(((ClassifyItemsOption) built.getFirst()).getKey()).isEqualTo("item-1");
      assertThat(((ClassifyItemsOption) built.get(1)).getKey()).isEqualTo("item-2");
    }

    @Test
    void ciItemsReceiveLabelsFromRequest() throws Exception {
      Level level = makeLevel(3, LevelType.CLASSIFY_ITEMS);
      when(levelRepository.findById(3)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), any())).thenReturn(ciOptions());

      service.createQuestion(classroomId, 3, ciCreateReq(), principal);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), any());
      ClassifyItemsOption first = (ClassifyItemsOption) captor.getValue().getFirst();
      assertThat(first.getLabels()).containsExactly("Safe", "Dangerous");
    }

    @Test
    void psOptionHasIdOne() throws Exception {
      Level level = makeLevel(4, LevelType.PASSWORD_STRENGTH);
      when(levelRepository.findById(4)).thenReturn(Optional.of(level));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), any())).thenReturn(psOptions());

      service.createQuestion(classroomId, 4, psCreateReq(), principal);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), any());
      PasswordStrengthOption ps = (PasswordStrengthOption) captor.getValue().getFirst();
      assertThat(ps.getId()).isEqualTo(1);
    }
  }

  @Nested
  class UpdateQuestion {

    @Test
    void updatesMultipleChoiceQuestion() throws Exception {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      Question existing = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(existing));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.MULTIPLE_CHOICE))).thenReturn(mcOptions());

      TeacherQuestionResponse resp =
          service.updateQuestion(classroomId, 1, 10, mcUpdateReq(), principal);

      assertThat(resp.questionText()).isEqualTo("MC updated?");
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherMultipleChoiceOptionResponse.class);
      verify(optionValidator).validate(anyList(), eq(LevelType.MULTIPLE_CHOICE));
    }

    @Test
    void updatesPictureQuizQuestion() throws Exception {
      Level level = makeLevel(2, LevelType.PICTURE_QUIZ);
      Question existing = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(existing));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.PICTURE_QUIZ))).thenReturn(pqOptions());

      TeacherQuestionResponse resp =
          service.updateQuestion(classroomId, 2, 10, pqUpdateReq(), principal);

      assertThat(resp.questionText()).isEqualTo("PQ updated?");
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherPictureQuizOptionResponse.class);
      verify(optionValidator).validate(anyList(), eq(LevelType.PICTURE_QUIZ));
    }

    @Test
    void updatesClassifyItemsQuestion() throws Exception {
      Level level = makeLevel(3, LevelType.CLASSIFY_ITEMS);
      Question existing = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(existing));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.CLASSIFY_ITEMS))).thenReturn(ciOptions());

      TeacherQuestionResponse resp =
          service.updateQuestion(classroomId, 3, 10, ciUpdateReq(), principal);

      assertThat(resp.questionText()).isEqualTo("CI updated?");
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherClassifyItemsOptionResponse.class);
      verify(optionValidator).validate(anyList(), eq(LevelType.CLASSIFY_ITEMS));
    }

    @Test
    void updatesPasswordStrengthQuestion() throws Exception {
      Level level = makeLevel(4, LevelType.PASSWORD_STRENGTH);
      Question existing = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(existing));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.PASSWORD_STRENGTH))).thenReturn(psOptions());

      TeacherQuestionResponse resp =
          service.updateQuestion(classroomId, 4, 10, psUpdateReq(), principal);

      assertThat(resp.questionText()).isEqualTo("PS updated?");
      assertThat(resp.options()).hasSize(1);
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherPasswordStrengthOptionResponse.class);

      ArgumentCaptor<List<Option>> captor = ArgumentCaptor.forClass(List.class);
      verify(optionValidator).validate(captor.capture(), eq(LevelType.PASSWORD_STRENGTH));
      PasswordStrengthOption ps = (PasswordStrengthOption) captor.getValue().getFirst();
      assertThat(ps.getMinLength()).isEqualTo(12);
      assertThat(ps.isRequireSymbol()).isTrue();
      assertThat(ps.getSuccessFeedback()).isEqualTo("Great!");
    }

    @Test
    void updatesTextQuizQuestion() throws Exception {
      Level level = makeLevel(5, LevelType.TEXT_QUIZ);
      Question existing = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(existing));
      stubSaveAndSerialize();
      when(optionParser.parse(any(), eq(LevelType.TEXT_QUIZ))).thenReturn(tqOptions());

      TeacherQuestionResponse resp =
          service.updateQuestion(classroomId, 5, 10, tqUpdateReq(), principal);

      assertThat(resp.questionText()).isEqualTo("TQ updated?");
      assertThat(resp.options().getFirst()).isInstanceOf(TeacherTextQuizOptionResponse.class);
      verify(optionValidator).validate(anyList(), eq(LevelType.TEXT_QUIZ));
    }

    @Test
    void throwsWhenQuestionNotFound() {
      when(questionRepository.findById(404)).thenReturn(Optional.empty());

      assertThatThrownBy(() ->
          service.updateQuestion(classroomId, 1, 404, mcUpdateReq(), principal)
      ).isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void throwsWhenLevelIdMismatch() {
      Level level = makeLevel(99, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(q));

      assertThatThrownBy(() ->
          service.updateQuestion(classroomId, 1, 10, mcUpdateReq(), principal)
      ).isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void throwsWhenEditingDefaultQuestion() {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, true, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(q));

      assertThatThrownBy(() ->
          service.updateQuestion(classroomId, 1, 10, mcUpdateReq(), principal)
      ).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("default");
    }

    @Test
    void throwsWhenClassroomMismatch() {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, false, UUID.randomUUID());
      when(questionRepository.findById(10)).thenReturn(Optional.of(q));

      assertThatThrownBy(() ->
          service.updateQuestion(classroomId, 1, 10, mcUpdateReq(), principal)
      ).isInstanceOf(AccessDeniedException.class)
          .hasMessageContaining("does not belong");
    }
  }

  @Nested
  class DeleteQuestion {

    @Test
    void deletesSuccessfully() {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(q));

      service.deleteQuestion(classroomId, 1, 10, principal);

      verify(questionRepository).delete(q);
    }

    @Test
    void throwsWhenDeletingDefault() {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, true, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(q));

      assertThatThrownBy(() ->
          service.deleteQuestion(classroomId, 1, 10, principal)
      ).isInstanceOf(IllegalArgumentException.class);

      verify(questionRepository, never()).delete(any());
    }

    @Test
    void throwsWhenClassroomMismatch() {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, false, UUID.randomUUID());
      when(questionRepository.findById(10)).thenReturn(Optional.of(q));

      assertThatThrownBy(() ->
          service.deleteQuestion(classroomId, 1, 10, principal)
      ).isInstanceOf(AccessDeniedException.class);

      verify(questionRepository, never()).delete(any());
    }

    @Test
    void throwsWhenLevelMismatch() {
      Level level = makeLevel(99, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findById(10)).thenReturn(Optional.of(q));

      assertThatThrownBy(() ->
          service.deleteQuestion(classroomId, 1, 10, principal)
      ).isInstanceOf(QuestionNotFoundException.class);

      verify(questionRepository, never()).delete(any());
    }

    @Test
    void throwsWhenNotFound() {
      when(questionRepository.findById(404)).thenReturn(Optional.empty());

      assertThatThrownBy(() ->
          service.deleteQuestion(classroomId, 1, 404, principal)
      ).isInstanceOf(QuestionNotFoundException.class);
    }
  }


  @Nested
  class ResponseMapping {

    @Test
    void mapsMcResponseFields() {
      Level level = makeLevel(1, LevelType.MULTIPLE_CHOICE);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findByLevelIdForClassroom(1, classroomId))
          .thenReturn(List.of(q));
      when(optionParser.parse(q.getOptionContent(), LevelType.MULTIPLE_CHOICE))
          .thenReturn(List.of(
              new MultipleChoiceOption(1, "A", "http://img.png", "http://resp.png", true, "Good")
          ));

      List<TeacherQuestionResponse> results =
          service.listQuestionsForTeacher(classroomId, 1, principal);

      TeacherMultipleChoiceOptionResponse opt =
          (TeacherMultipleChoiceOptionResponse) results.getFirst().options().getFirst();
      assertThat(opt.id()).isEqualTo(1);
      assertThat(opt.text()).isEqualTo("A");
      assertThat(opt.imageUrl()).isEqualTo("http://img.png");
      assertThat(opt.responseImageUrl()).isEqualTo("http://resp.png");
      assertThat(opt.isCorrect()).isTrue();
      assertThat(opt.explanation()).isEqualTo("Good");
    }

    @Test
    void mapsPqResponseFields() {
      Level level = makeLevel(2, LevelType.PICTURE_QUIZ);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findByLevelIdForClassroom(2, classroomId))
          .thenReturn(List.of(q));
      when(optionParser.parse(q.getOptionContent(), LevelType.PICTURE_QUIZ))
          .thenReturn(List.of(
              new PictureQuizOption(1, "Img", "http://pic.png", true, "Correct!")
          ));

      List<TeacherQuestionResponse> results =
          service.listQuestionsForTeacher(classroomId, 2, principal);

      TeacherPictureQuizOptionResponse opt =
          (TeacherPictureQuizOptionResponse) results.getFirst().options().getFirst();
      assertThat(opt.id()).isEqualTo(1);
      assertThat(opt.text()).isEqualTo("Img");
      assertThat(opt.imageUrl()).isEqualTo("http://pic.png");
      assertThat(opt.isCorrect()).isTrue();
      assertThat(opt.explanation()).isEqualTo("Correct!");
    }

    @Test
    void mapsCiResponseFields() {
      Level level = makeLevel(3, LevelType.CLASSIFY_ITEMS);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findByLevelIdForClassroom(3, classroomId))
          .thenReturn(List.of(q));
      when(optionParser.parse(q.getOptionContent(), LevelType.CLASSIFY_ITEMS))
          .thenReturn(List.of(
              new ClassifyItemsOption("item-1", "Item", List.of("A", "B"), "A", "Reason")
          ));

      List<TeacherQuestionResponse> results =
          service.listQuestionsForTeacher(classroomId, 3, principal);

      TeacherClassifyItemsOptionResponse opt =
          (TeacherClassifyItemsOptionResponse) results.getFirst().options().getFirst();
      assertThat(opt.key()).isEqualTo("item-1");
      assertThat(opt.text()).isEqualTo("Item");
      assertThat(opt.labels()).containsExactly("A", "B");
      assertThat(opt.correctLabel()).isEqualTo("A");
      assertThat(opt.explanation()).isEqualTo("Reason");
    }

    @Test
    void mapsPsResponseFields() {
      Level level = makeLevel(4, LevelType.PASSWORD_STRENGTH);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findByLevelIdForClassroom(4, classroomId))
          .thenReturn(List.of(q));
      when(optionParser.parse(q.getOptionContent(), LevelType.PASSWORD_STRENGTH))
          .thenReturn(List.of(
              new PasswordStrengthOption(1, 10, true, false, true, false, null, "Ok!", "Bad!")
          ));

      List<TeacherQuestionResponse> results =
          service.listQuestionsForTeacher(classroomId, 4, principal);

      TeacherPasswordStrengthOptionResponse opt =
          (TeacherPasswordStrengthOptionResponse) results.getFirst().options().getFirst();
      assertThat(opt.id()).isEqualTo(1);
      assertThat(opt.minLength()).isEqualTo(10);
      assertThat(opt.requireDigit()).isTrue();
      assertThat(opt.requireUpper()).isFalse();
      assertThat(opt.requireLower()).isTrue();
      assertThat(opt.requireSymbol()).isFalse();
      assertThat(opt.successFeedback()).isEqualTo("Ok!");
      assertThat(opt.failureFeedback()).isEqualTo("Bad!");
    }

    @Test
    void mapsTqResponseFields() {
      Level level = makeLevel(5, LevelType.TEXT_QUIZ);
      Question q = makeQuestion(10, level, false, classroomId);
      when(questionRepository.findByLevelIdForClassroom(5, classroomId))
          .thenReturn(List.of(q));
      when(optionParser.parse(q.getOptionContent(), LevelType.TEXT_QUIZ))
          .thenReturn(List.of(
              new MultipleChoiceOption(1, "Answer", null, null, true, "Right")
          ));

      List<TeacherQuestionResponse> results =
          service.listQuestionsForTeacher(classroomId, 5, principal);

      TeacherTextQuizOptionResponse opt =
          (TeacherTextQuizOptionResponse) results.getFirst().options().getFirst();
      assertThat(opt.id()).isEqualTo(1);
      assertThat(opt.text()).isEqualTo("Answer");
      assertThat(opt.isCorrect()).isTrue();
      assertThat(opt.explanation()).isEqualTo("Right");
    }
  }
}