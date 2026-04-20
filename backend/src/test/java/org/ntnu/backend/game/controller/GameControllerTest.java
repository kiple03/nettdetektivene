package org.ntnu.backend.game.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.MultipleChoiceOptionResponse;
import org.ntnu.backend.game.dto.OptionResponse;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.service.GameService;
import org.ntnu.backend.game.service.QuestionService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.ntnu.backend.security.JwtAuthenticationToken;
import org.ntnu.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GameService gameService;

  @MockitoBean
  private QuestionService questionService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private AuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @BeforeEach
  void setUp() {
    Collection<SimpleGrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));

    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        "student@test.no", UUID.randomUUID(), "ROLE_STUDENT", null, authorities);

    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(principal, authorities));
  }

  // ===== GET /game/stops =====

  @Test
  void getStops_returnsListOfStops() throws Exception {
    when(gameService.getStopsForUser(any())).thenReturn(List.of(
        new StopResponse(1, "Nyhetskvartalet", false),
        new StopResponse(2, "Fotografen", false)
    ));

    mockMvc.perform(get("/game/stops"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Nyhetskvartalet"))
        .andExpect(jsonPath("$[1].name").value("Fotografen"));
  }

  @Test
  void getStops_emptyDb_returnsEmptyList() throws Exception {
    when(gameService.getStopsForUser(any())).thenReturn(List.of());

    mockMvc.perform(get("/game/stops"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // ===== GET /game/stops/{stopId}/levels =====

  @Test
  void getLevelsForStop_returnsLevels() throws Exception {
    when(gameService.getLevelsForStop(eq(1), any())).thenReturn(List.of(
        new LevelResponse(1, "Ekte eller falskt?", "Finn den ekte nyheten",
            LevelType.MULTIPLE_CHOICE, false),
        new LevelResponse(2, "Clickbait-detektiven", "Spot clickbait",
            LevelType.MULTIPLE_CHOICE, false)
    ));

    mockMvc.perform(get("/game/stops/1/levels"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].title").value("Ekte eller falskt?"))
        .andExpect(jsonPath("$[1].title").value("Clickbait-detektiven"));
  }

  // ===== GET /game/levels/{levelId} =====

  @Test
  void getLevel_returnsLevelDetail() throws Exception {
    when(gameService.getLevel(eq(1), any())).thenReturn(
        new LevelResponse(1, "Ekte eller falskt?", "Finn den ekte nyheten",
            LevelType.MULTIPLE_CHOICE, false));

    mockMvc.perform(get("/game/levels/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Ekte eller falskt?"))
        .andExpect(jsonPath("$.levelType").value("MULTIPLE_CHOICE"))
        .andExpect(jsonPath("$.locked").value(false));
  }

  // ===== GET /game/levels/{levelId}/questions =====

  @Test
  void getQuestionsForLevel_returnsQuestions() throws Exception {
    List<OptionResponse> options = List.of(
        new MultipleChoiceOptionResponse(1, "Option A", "img1.jpg"),
        new MultipleChoiceOptionResponse(2, "Option B", "img2.jpg")
    );

    when(questionService.getQuestionsForLevel(1)).thenReturn(List.of(
        new QuestionResponse(1, LevelType.MULTIPLE_CHOICE, "Which is real?", options)
    ));

    mockMvc.perform(get("/game/levels/1/questions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].questionText").value("Which is real?"))
        .andExpect(jsonPath("$[0].options.length()").value(2));
  }

  @Test
  void getQuestionsForLevel_optionsDontContainAnswers() throws Exception {
    List<OptionResponse> options = List.of(
        new MultipleChoiceOptionResponse(1, "Option A", "img1.jpg")
    );

    when(questionService.getQuestionsForLevel(1)).thenReturn(List.of(
        new QuestionResponse(1, LevelType.MULTIPLE_CHOICE, "Test?", options)
    ));

    mockMvc.perform(get("/game/levels/1/questions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].options[0].isCorrect").doesNotExist())
        .andExpect(jsonPath("$[0].options[0].explanation").doesNotExist());
  }

  // ===== GET /game/questions/{questionId} =====

  @Test
  void getQuestion_returnsSingleQuestion() throws Exception {
    List<OptionResponse> options = List.of(
        new MultipleChoiceOptionResponse(1, "A", "img.jpg"),
        new MultipleChoiceOptionResponse(2, "B", "img.jpg")
    );

    when(questionService.getQuestionById(1)).thenReturn(
        new QuestionResponse(1, LevelType.MULTIPLE_CHOICE, "Which?", options));

    mockMvc.perform(get("/game/questions/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.questionText").value("Which?"))
        .andExpect(jsonPath("$.options.length()").value(2));
  }

  // ===== POST /game/questions/{questionId}/submit =====

  @Test
  void submitAnswer_correctAnswer_returnsTrue() throws Exception {
    when(questionService.submitAnswer(eq(1), any()))
        .thenReturn(new SubmitAnswerResponse(true, "Correct!"));

    mockMvc.perform(post("/game/questions/1/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"shape\": \"SINGLE\", \"selectedId\": 2}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(true))
        .andExpect(jsonPath("$.feedback").value("Correct!"));
  }

  @Test
  void submitAnswer_wrongAnswer_returnsFalse() throws Exception {
    when(questionService.submitAnswer(eq(1), any()))
        .thenReturn(new SubmitAnswerResponse(false, "Wrong!"));

    mockMvc.perform(post("/game/questions/1/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"shape\": \"SINGLE\", \"selectedId\": 1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(false))
        .andExpect(jsonPath("$.feedback").value("Wrong!"));
  }

  @Test
  void submitAnswer_missingBody_returnsError() throws Exception {
    mockMvc.perform(post("/game/questions/1/submit")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError());
  }
}