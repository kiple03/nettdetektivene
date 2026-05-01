package org.ntnu.backend.game.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.config.openapi.ApiErrorResponse;
import org.ntnu.backend.game.service.GameService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-only cheat endpoint. Only registered when APP_CHEATS_ENABLED=true.
 * Not available in production.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
@ConditionalOnProperty(name = "app.cheats.enabled", havingValue = "true")
@Tag(name = "Admin Cheats", description = "Dev-only cheat endpoints (requires app.cheats.enabled=true)")
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
  @Operation(
      summary = "Force-complete a level",
      description = "Marks a level as completed for the authenticated user, bypassing answer validation. Only available when app.cheats.enabled=true."
  )
  @ApiResponse(responseCode = "204", description = "Level marked as completed")
  @ApiErrorResponse(responseCode = "401", description = "Not authenticated")
  @ApiErrorResponse(responseCode = "404", description = "Level not found")
  public ResponseEntity<Void> adminCompleteLevel(
      @PathVariable int levelId,
      @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    gameService.adminCompleteLevel(levelId, principal);
    return ResponseEntity.noContent().build();
  }
}
