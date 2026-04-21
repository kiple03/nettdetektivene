package org.ntnu.backend.avatar.controller;

import java.util.List;

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
public class AvatarController {

    private final UserAvatarService userAvatarService;
    
    /**
     * GET-request for avatar functionality. Gets the users profile and ONLY 
     * the items the user is already wearing.
     * @param principal jwt tokens for security.
     * @return a responseEntity of the users profile and current avatar items.
     */
    @GetMapping("/user/profile")
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
    public ResponseEntity<Void> saveOutfit(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal, @RequestBody UpdateEquippedItemsRequest request){
        userAvatarService.updateEquippedItems(principal.getUserId(), request.getItemIds());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
        @AuthenticationPrincipal JwtAuthenticatedPrincipal principal,
        @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileResponse updated = userAvatarService.updateUserProfile(principal.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

}
