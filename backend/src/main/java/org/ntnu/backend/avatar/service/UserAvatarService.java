package org.ntnu.backend.avatar.service;

import java.util.List;
import java.util.UUID;

import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.dto.UserProfileResponse;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAvatarService {
    
    private final UserInventoryItemRepository inventoryRepository;
    private final UserRepository userRepository;

    public UserProfileResponse getUserProfile(UUID userid){
        User user = userRepository.findById(userid).orElseThrow(() -> new RuntimeException("User not found"));

        List<UserInventoryItem> equippedItems = inventoryRepository.findByUserIdAndIsEquippedTrue(userid);
        
        List<EquippedItemDto> itemDtos = equippedItems.stream()
                                        .map(inventoryItem -> EquippedItemDto.builder()
                                            .id(inventoryItem.getAvatarItem().getId())
                                            .name(inventoryItem.getAvatarItem().getName())
                                            .imageUrl("/assets/avatar-parts/" + inventoryItem.getAvatarItem().getName() + ".png")
                                            .build())
                                        .toList();
        return UserProfileResponse.builder()
                .username(user.getUsername())
                .equippedItems(itemDtos)
                .build();
    }
}
