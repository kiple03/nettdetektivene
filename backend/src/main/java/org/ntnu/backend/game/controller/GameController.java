package org.ntnu.backend.game.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.config.openapi.ApiErrorResponse;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.OptionResponse;
import org.ntnu.backend.game.dto.QuestionResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.dto.progress.SubmitLevelRequest;
import org.ntnu.backend.game.model.enums.LevelType;
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
@Tag(name = "Game", description = "Student-facing game flow: map, levels, questions, and submissions.")
public class GameController {
  private final GameService gameService;
  private final QuestionService questionService;

  /**
   * Returns all stops for the game map, ordered by display position.
   * Each stop includes its lock state for the current student.
   */
  @GetMapping("/stops")
  @Operation(
      summary = "Get all stops with lock state for the current user",
      description = "Returns every stop in the game, ordered by display position. "
          + "Each stop's locked field reflects whether the authenticated user can access it. "
          + "Teachers receive everything unlocked; students see stops up to and including their current stop."
  )
  @ApiResponse(responseCode = "200", description = "List of stops with per-user lock state")
  public List<StopResponse> getGameStops(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return gameService.getStopsForUser(principal.getUserId(), principal.getRole());

  }

  /**
   * Returns all levels for a given stop, ordered by ID.
   * Each level includes its lock state for the current student.
   */
  @Operation(
      summary = "Gets all levels for a given stop",
      description = "Returns all levels in a given stop by its id. "
          + "Ordered by their display order. The locked field reflects "
          + "if the user can access this level"
  )
  @GetMapping("/stops/{stopId}/levels")
  public List<LevelResponse> getLevelsForStop(
      @PathVariable int stopId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return gameService.getLevelsForStop(stopId, principal.getUserId(), principal.getRole());
  }

  /**
   * Returns all questions for a given level. Options are included
   * but answer data (correct flags, explanations) is excluded.
   * The frontend shows these one at a time and controls the flow locally.
   */
  @Operation(
      summary = "Gets all questions for a given level",
      description = "Returns all questions in a given level by the levels id. "
  )
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
  @Operation(
      summary = "Get a single levels data"
  )
  @ApiErrorResponse(responseCode = "404", description = "Level was not found")
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
  @Operation(
      summary = "Get a single questions data, with all the answer options"
  )
  @ApiErrorResponse(responseCode = "404", description = "Question was not found")
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
  @Operation(
      summary = "Validate if the answer is correct",
      description = "Validates if the answer to a specific question is correct. "
          + "Specifies the questions by the id."
  )
  @ApiErrorResponse(responseCode = "404", description = "Question was not found")
  @PostMapping("/questions/{questionId}/submit")
  public SubmitAnswerResponse submitAnswer(
      @PathVariable int questionId,
      @RequestBody SubmitAnswerRequest request,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return questionService.submitAnswer(questionId, request);
  }

  @Operation(
      summary = "Validates if all answers in a level is correct",
      description = "To track progress, a level must be completed. This can only be done "
          + "with validating all answers to all the levels question is correct. "
          + "Should only be called when a user has all the right answers to a level"
  )
  @ApiResponse(responseCode = "200", description = "All correct answers, user progress is updated")
  @ApiErrorResponse(responseCode = "400", description = "Malformed list of answers")
  @ApiErrorResponse(responseCode = "404", description = "Level was not found")
  @ApiErrorResponse(responseCode = "500", description = "User has no progression relation in db")
  @PostMapping("/levels/{levelId}/submit")
  public ResponseEntity<Void> submitLevel(
      @PathVariable int levelId,
      @RequestBody SubmitLevelRequest request,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    gameService.submitLevel(levelId, request, principal.getUserId(), principal.getRole());
    return ResponseEntity.noContent().build();
  }
}
