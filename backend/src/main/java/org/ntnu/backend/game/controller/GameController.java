package org.ntnu.backend.game.controller;


import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.LevelResponse;
import org.ntnu.backend.game.dto.StopOverviewResponse;
import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.SubmitAnswerResponse;
import org.ntnu.backend.game.service.ClassroomAuthorizationService;
import org.ntnu.backend.game.service.GameService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("classrooms/{classroomId}")
public class GameController {
  private final GameService gameService;
  private final ClassroomAuthorizationService authService;

  @GetMapping("/stops")
  public List<StopOverviewResponse> getGameStops(@PathVariable UUID classroomId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    authService.verifyAccess(principal, classroomId);
    return gameService.getStopsForUser(classroomId, principal.getUserId());

  }

  @GetMapping("/levels/{levelId}")
  public LevelResponse getLevel(
      @PathVariable UUID classroomId,
      @PathVariable UUID levelId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    authService.verifyAccess(principal, classroomId);
    return gameService.getLevel(classroomId, levelId, principal.getUserId());
  }

  @PostMapping("/questions/{questionId}/submit")
  public SubmitAnswerResponse submitAnswer(
      @PathVariable UUID classroomId,
      @PathVariable UUID questionId,
      @RequestBody SubmitAnswerRequest request,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    authService.verifyAccess(principal, classroomId);
    return gameService.submitAnswer(classroomId, questionId, request, principal.getUserId());
  }
}
