package org.ntnu.backend.game.controller;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.service.GameService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Dev-only cheat endpoint. Only registered when APP_CHEATS_ENABLED=true.
 * Not available in production.
 */
//@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
@ConditionalOnProperty(name = "app.cheats.enabled", havingValue = "true")
public class AdminCheatController {

  private final GameService gameService;

  /**
   * Marks a level as completed for the authenticated user, bypassing answer validation.
   *
   * @param levelId   the level to force-complete
   * @param principal the authenticated user
   * @return 204 No Content on success
   */
  @PostMapping("/levels/{levelId}/admin-complete")
  public ResponseEntity<Void> adminCompleteLevel(
      @PathVariable int levelId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    gameService.adminCompleteLevel(levelId, principal);
    return ResponseEntity.noContent().build();
  }
}
