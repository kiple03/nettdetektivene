package org.ntnu.backend.avatar.controller;

import java.util.List;

import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.dto.UpdateEquippedItemsRequest;
import org.ntnu.backend.avatar.dto.UserProfileResponse;
import org.ntnu.backend.avatar.service.UserAvatarService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AvatarController {

    private final UserAvatarService userAvatarService;
    
    @GetMapping("/user/profile")
    public ResponseEntity<UserProfileResponse> getUser(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal){
        UserProfileResponse avatar = userAvatarService.getUserProfile(principal.getUserId());
        return ResponseEntity.ok(avatar);
    }

    @GetMapping("/user/inventory")
    public ResponseEntity<List<EquippedItemDto>> getInventory(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
        return ResponseEntity.ok(userAvatarService.getUserInventory(principal.getUserId()));
    }

    @PutMapping("/user/avatar/equip")
    public ResponseEntity<Void> saveOutfit(@AuthenticationPrincipal JwtAuthenticatedPrincipal principal, @RequestBody UpdateEquippedItemsRequest request){
        userAvatarService.updateEquippedItems(principal.getUserId(), request.getItemIds());
        return ResponseEntity.ok().build();
    }
}
