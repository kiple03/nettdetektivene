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
import org.springframework.transaction.annotation.Transactional;
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
                                            .imageUrl("/assets/" + inventoryItem.getAvatarItem().getName() + ".png")
                                            .build())
                                        .toList();
        return UserProfileResponse.builder()
                .username(user.getUsername())
                .equippedItems(itemDtos)
                .build();
    }

    @Transactional
    public void updateEquippedItems(UUID userId, List<UUID> itemIdsToEquip){
        // Get all items in users inventory
        List<UserInventoryItem> inventory = inventoryRepository.findByUserId(userId);

        for(UserInventoryItem item : inventory){
            UUID id = item.getAvatarItem().getId();
            // If item ID found in users list, equip it!
            boolean shouldBeEquipped = itemIdsToEquip != null && itemIdsToEquip.contains(id);
            item.setEquipped(shouldBeEquipped);
        }
        // save updated inventory to database
        inventoryRepository.saveAll(inventory);
    }

        public List<EquippedItemDto> getUserInventory(UUID userId) {
        List<UserInventoryItem> inventory = inventoryRepository.findByUserId(userId);
        
        return inventory.stream()
            .map(item -> EquippedItemDto.builder()
                .id(item.getAvatarItem().getId())
                .name(item.getAvatarItem().getName())
                .category(item.getAvatarItem().getCategory()) // Make sure EquippedItemDto has a category field!
                .imageUrl("/assets/" + item.getAvatarItem().getName() + ".svg")
                .build())
            .toList();
    }
}
