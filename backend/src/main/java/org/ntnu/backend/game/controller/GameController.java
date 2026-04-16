package org.ntnu.backend.game.controller;


import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.StopResponse;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.service.GameService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
  private final GameService gameService;
  private final ClassroomAuthorizationService authService;

  @GetMapping("/stops")
  public List<StopResponse> getGameStops(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return gameService.getStopsForUser(principal.getUserId());

  }

  @GetMapping("/levels/{levelId}")
  public LevelResponse getLevel(
      @PathVariable int levelId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    return gameService.getLevel(levelId, principal.getUserId());
  }

//  @PostMapping("/questions/{questionId}/submit")
//  public SubmitAnswerResponse submitAnswer(
//      @PathVariable UUID questionId,
//      @RequestBody SubmitAnswerRequest request,
//      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
//    return gameService.submitAnswer(questionId, request, principal.getUserId());
//  }
}
