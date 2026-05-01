package org.ntnu.backend.game.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.config.openapi.ApiErrorResponse;
import org.ntnu.backend.game.dto.bossbattle.BossBattleResultResponse;
import org.ntnu.backend.game.dto.bossbattle.BossBattleSaveRequest;
import org.ntnu.backend.game.service.BossBattleService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for recording and retrieving boss battle outcomes.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/game/boss")
@Tag(name = "Boss Battle", description = "Record and retrieve boss battle outcomes")
public class BossBattleController {

    private final BossBattleService bossBattleService;

    /**
     * Persists the outcome of a boss battle for the current user and classroom.
     *
     * @param request   the battle result payload
     * @param principal the authenticated student
     * @return 201 Created on success
     */
    @PostMapping("/result")
    @Operation(
        summary = "Save boss battle result",
        description = "Persists the outcome of a boss battle for the authenticated student's current classroom."
    )
    @ApiResponse(responseCode = "201", description = "Result saved successfully")
    @ApiErrorResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<Void> saveResult(
        @Valid @RequestBody BossBattleSaveRequest request,
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        bossBattleService.saveResult(principal.getUserId(), principal.getClassroomId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Retrieves the most recent boss battle result for the current user and classroom.
     *
     * @param principal the authenticated student
     * @return 200 with the latest result, or 404 if none exists
     */
    @GetMapping("/result")
    @Operation(
        summary = "Get latest boss battle result",
        description = "Retrieves the most recent boss battle result for the authenticated student in their current classroom."
    )
    @ApiErrorResponse(responseCode = "404", description = "No boss battle result found")
    public ResponseEntity<BossBattleResultResponse> getLatestResult(
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return bossBattleService
            .getLatestResult(principal.getUserId(), principal.getClassroomId())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
