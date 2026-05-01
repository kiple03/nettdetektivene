package org.ntnu.backend.avatar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.dto.UpdateEquippedItemsRequest;
import org.ntnu.backend.avatar.dto.UpdateUserProfileRequest;
import org.ntnu.backend.avatar.dto.UserProfileResponse;
import org.ntnu.backend.avatar.service.UserAvatarService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;


/**
 * REST controller. Assign endpoints for HTTP traffic to backend avatar-functionality. 
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Avatar", description = "User profile's avatar management")
public class AvatarController {

    private final UserAvatarService userAvatarService;
    
    /**
     * GET-request for avatar functionality. Gets the users profile and ONLY 
     * the items the user is already wearing.
     * @param principal jwt tokens for security.
     * @return a responseEntity of the users profile and current avatar items.
     */
    @GetMapping("/user/profile")
    @Operation(summary = "Get current user profile with equipped items")
    public ResponseEntity<UserProfileResponse> getUser(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal){
        UserProfileResponse avatar = userAvatarService.getUserProfile(principal.getUserId());
        return ResponseEntity.ok(avatar);
    }

    /**
     * GET-request for avatar functionality. Fetches every single item the user OWNS, regardless of it being equipped or not.
     * @param principal jwt authentication.
     * @return a list of every avatar item associated with the user. 
     */
    @GetMapping("/user/inventory")
    @Operation(summary = "Get all items owned by the user")
    public ResponseEntity<List<EquippedItemDto>> getInventory(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return ResponseEntity.ok(userAvatarService.getUserInventory(principal.getUserId()));
    }

    /**
     * Put-request. Puts a users local avatar changes into backends database. 
     * @param principal jwt autentication
     * @param request the items requested to be stored in database 
     * @return an ok-signal. 
     */
    @PutMapping("/user/avatar/equip")
    @Operation(summary = "Equip selected avatar items")
    @ApiResponse(responseCode = "200", description = "Outfit saved")
    public ResponseEntity<Void> saveOutfit(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal, @RequestBody UpdateEquippedItemsRequest request){
        userAvatarService.updateEquippedItems(principal.getUserId(), request.getItemIds());
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the authenticated user's profile (e.g. username).
     *
     * @param principal the authenticated user's JWT principal
     * @param request   the profile fields to update
     * @return a new access token reflecting the updated profile claims
     */
    @PutMapping("/user/profile")
    @Operation(
        summary = "Update user profile",
        description = "Updates username and returns a new access token with updated claims"
    )
    @ApiResponse(responseCode = "200", description = "Updated user, new accessToken in body")
    public ResponseEntity<Map<String, String>> updateUserProfile(
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal,
        @Valid @RequestBody UpdateUserProfileRequest request) {
        String accessToken = userAvatarService.updateUserProfile(principal.getUserId(), principal.getClassroomId(), request);
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

}
