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

/**
 * REST controller for medalion functionality.
 * Exposes endpoints for awarding and fetching medalions tied to the authenticated user.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/medalions")
public class MedalionController {
    private final MedalionService medalionService;

    /**
     * Awards a new medalion to a user.
     *
     * @param principal the authenticated user's JWT principal
     * @param request the request body containing medalion name and description
     * @return the awarded {@link MedalionResponse}
     */
    @PostMapping
    public ResponseEntity<MedalionResponse> awardUser(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal, @Valid @RequestBody AwardMedalionRequest request){
        return ResponseEntity.ok(medalionService.awardMedalion(request));
    }

    /**
     * Retrieves all medalions earned by the authenticated user.
     *
     * @param principal the authenticated user's JWT principal
     * @return a list of {@link MedalionResponse} belonging to the user
     */
    @GetMapping
    public ResponseEntity<List<MedalionResponse>> getMedalions(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal){
        return ResponseEntity.ok(medalionService.getAllUsersMedalion(principal.getUserId()));
    }

}
