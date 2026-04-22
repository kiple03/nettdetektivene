package org.ntnu.backend.medalion.controller;

import java.util.List;
import org.ntnu.backend.medalion.dto.AwardMedalionRequest;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/medalions")
public class MedalionController {
    private final MedalionService medalionService;

    @PostMapping
    public ResponseEntity<MedalionResponse> awardUser(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal, @Valid @RequestBody AwardMedalionRequest request){
        return ResponseEntity.ok(medalionService.awardMedalion(request));
    }

    @GetMapping
    public ResponseEntity<List<MedalionResponse>> getMedalions(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal){
        return ResponseEntity.ok(medalionService.getAllUsersMedalion(principal.getUserId()));
    }

}
