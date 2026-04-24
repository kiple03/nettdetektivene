package org.ntnu.backend.game.controller;

import lombok.RequiredArgsConstructor;
import org.ntnu.backend.game.dto.BossBattleResultResponse;
import org.ntnu.backend.game.dto.BossBattleSaveRequest;
import org.ntnu.backend.game.service.BossBattleService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game/boss")
public class BossBattleController {

    private final BossBattleService bossBattleService;

    @PostMapping("/result")
    public ResponseEntity<Void> saveResult(
        @Valid @RequestBody BossBattleSaveRequest request,
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        bossBattleService.saveResult(principal.getUserId(), principal.getClassroomId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/result")
    public ResponseEntity<BossBattleResultResponse> getLatestResult(
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal
    ) {
        return bossBattleService
            .getLatestResult(principal.getUserId(), principal.getClassroomId())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
