package org.ntnu.backend.game.controller;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.OptionResponse;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.dto.progress.SubmitLevelRequest;
import org.ntnu.backend.game.model.enums.LevelType;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.service.GameService;
import org.ntnu.backend.game.service.QuestionService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Student-facing game endpoints. Provides the read and submit flow:
 * fetch stops for the map, then gets the levels to the stop, and then the user can
 * view a question, and submit an answer for feedback.
 *
 * <p>All endpoints require authentication. Stops and levels are global
 * no classroom scoping. Questions are both global and classroom specific (not implemented).
 *
 * @see GameService
 * @see QuestionService
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
  private final GameService gameService;
  private final QuestionService questionService;

  /**
   * Returns all stops for the game map, ordered by display position.
   * Each stop includes its lock state for the current student.
   */
  @GetMapping("/stops")
  public List<StopResponse> getGameStops(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return gameService.getStopsForUser(principal.getUserId(), principal.getRole()));

  }

  /**
   * Returns all levels for a given stop, ordered by ID.
   * Each level includes its lock state for the current student.
   */
  @GetMapping("/stops/{stopId}/levels")
  public List<LevelResponse> getLevelsForStop(
      @PathVariable int stopId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return gameService.getLevelsForStop(stopId, principal.getUserId(), principal.getRole()));
  }

  /**
   * Returns all questions for a given level. Options are included
   * but answer data (correct flags, explanations) is excluded.
   * The frontend shows these one at a time and controls the flow locally.
   */
  @GetMapping("/levels/{levelId}/questions")
  public List<QuestionResponse> getQuestionsForLevel(
      @PathVariable int levelId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return questionService.getQuestionsForLevel(levelId);
  }

  /**
   * Returns a single level's metadata including title, description,
   * level type, and lock state. Used for the guidance screen shown
   * before the student starts answering questions.
   */
  @GetMapping("/levels/{levelId}")
  public LevelResponse getLevel(
      @PathVariable int levelId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return gameService.getLevel(levelId, principal.getUserId(), principal.getRole());
  }


  /**
   * Returns a single question with its options. Answer data (correct
   * flags and explanations) is excluded from the response by the
   * sealed {@link OptionResponse} hierarchy.
   */
  @GetMapping("/questions/{questionId}")
  public QuestionResponse getQuestion(
      @PathVariable int questionId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
  ) {
    return questionService.getQuestionById(questionId);
  }


  /**
   * Validates a student's submitted answer and returns feedback.
   *
   * <p>The request body is polymorphic, tagged by {@code shape}
   * to match the question's type. For example, a
   * {@link LevelType#MULTIPLE_CHOICE} question expects:
   * {@code {"shape": "SINGLE", "selectedId": int}}
   *
   * @return whether the answer is correct, with an explanation
   */
  @PostMapping("/questions/{questionId}/submit")
  public SubmitAnswerResponse submitAnswer(
      @PathVariable int questionId,
      @RequestBody SubmitAnswerRequest request,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return questionService.submitAnswer(questionId, request);
  }


  @PostMapping("/levels/{levelId}/submit")
  public ResponseEntity<Void> submitLevel(
      @PathVariable int levelId,
      @RequestBody SubmitLevelRequest request,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    gameService.submitLevel(levelId, request, principal.getUserId(), principal.getRole());
    return ResponseEntity.noContent().build();
  }
}
