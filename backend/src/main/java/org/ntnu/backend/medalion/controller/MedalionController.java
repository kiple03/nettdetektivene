package org.ntnu.backend.medalion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

/**
 * REST controller for medalion functionality.
 * Exposes endpoints for awarding and fetching medalions tied to the authenticated user.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/medalions")
@Tag(name = "Medallions", description = "Earning and viewing achievement medallions")
public class MedalionController {
    private final MedalionService medalionService;

    /**
     * Awards a new medalion to a user.
     *
     * @param principal the authenticated user's JWT principal
     * @param request the request body containing medalion name and description
     * @return the awarded {@link MedalionResponse}
     */
    @Operation(
        summary = "Award a medallion to a user",
        description = "Grants a medallion based on the request body. Called internally when a student completes a stop"
    )
    @PostMapping
    public ResponseEntity<MedalionResponse> awardUser(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal, @Valid @RequestBody AwardMedalionRequest request){
        return ResponseEntity.ok(medalionService.awardMedalion(request));
    }

    /**
     * Retrieves all medallions earned by the authenticated user.
     *
     * @param principal the authenticated user's JWT principal
     * @return a list of {@link MedalionResponse} belonging to the user
     */
    @GetMapping
    @Operation(summary = "Get all medallions earned by the authenticated user")
    public ResponseEntity<List<MedalionResponse>> getMedalions(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal){
        return ResponseEntity.ok(medalionService.getAllUsersMedalion(principal.getUserId()));
    }

}
