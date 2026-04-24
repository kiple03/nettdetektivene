package org.ntnu.backend.avatar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.dto.UpdateUserProfileRequest;
import org.ntnu.backend.avatar.dto.UserProfileResponse;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.classroom.service.NotificationService;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

/**
 * Service class for the logic of avatars. It actively talks to the database using the 
 * repositories, and formats database data for frontend. 
 */
@Service
@RequiredArgsConstructor
public class UserAvatarService {
    
    private final UserInventoryItemRepository inventoryRepository;
    private final AvatarItemRepository avatarItemRepository;
    private final UserRepository userRepository;
    private final ProfanityFilterService profanityFilter;
    private final NotificationService notificationService;

    /**
     * Service-method that gets the user by id, and retrieves only the items the user has equipped.
     * @param userid the users id
     * @return a user profile response that contains user name and avatar items. 
     */
    public UserProfileResponse getUserProfile(UUID userid){
        User user = userRepository.findById(userid).orElseThrow(() -> new RuntimeException("User not found"));

        List<UserInventoryItem> equippedItems = inventoryRepository.findByUserIdAndIsEquippedTrue(userid);
        
        List<EquippedItemDto> itemDtos = equippedItems.stream()
                                        .map(inventoryItem -> EquippedItemDto.builder()
                                            .id(inventoryItem.getAvatarItem().getId())
                                            .name(inventoryItem.getAvatarItem().getName())
                                            .category(inventoryItem.getAvatarItem().getCategory())
                                            .imageUrl("/assets/" + inventoryItem.getAvatarItem().getName() + ".svg")
                                            .build())
                                        .toList();
        
        return UserProfileResponse.builder()
            .username(user.getUsername())
            .equippedItems(itemDtos)
            .build();
    }

    /**
     * Service-method that updates the chosen item to the user. 
     * @param userId user identification.
     * @param itemIdsToEquip item identification.
     */
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

    /**
     * Service-method that gets the whole users inventory, regardless of it being equipped or not.
     * @param userId user identification.
     * @return a list of all the avatar items accociated with the user.
     */
    public List<EquippedItemDto> getUserInventory(UUID userId) {
        List<UserInventoryItem> inventory = inventoryRepository.findByUserIdOrderByAvatarItemNameAsc(userId);

        return inventory.stream()
            .map(item -> EquippedItemDto.builder()
                .id(item.getAvatarItem().getId())
                .name(item.getAvatarItem().getName())
                .category(item.getAvatarItem().getCategory()) // Make sure EquippedItemDto has a category field!
                .imageUrl("/assets/" + item.getAvatarItem().getName() + ".svg")
                .build())
            .toList();
    }

    /**
     * Assigns starter items for a new user. This comes from the Avatar data Seeder in config-folder.
     * @param user the user instance. 
     */
    @Transactional
    public void starterItems(User user){
        List<AvatarItem> starterItems = avatarItemRepository.findAll().stream()
            .filter(item -> List.of("detective_hat", "detective_suit", "face_1", "face_2", "face_3", "magnifying_glass", "police_hat", "police_uniform", "sheriff_hat", "sheriff_star", "sheriff_suit", "stop_sign", "tone_light", "tone_medium", "tone_dark", "tone_wacky").contains(item.getName()))
            .toList();

        List<UserInventoryItem> startingInventory = starterItems.stream().map(item -> 
            UserInventoryItem.builder()
                .user(user)
                .avatarItem(item)
                .unlockedAt(LocalDateTime.now())
                .isEquipped(List.of("detective_hat", "detective_suit", "face_1", "tone_light").contains(item.getName()))
                .build()
        ).toList();

        inventoryRepository.saveAll(startingInventory);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String newUsername = request.username().trim();

        if (profanityFilter.containsProfanity(newUsername)) {
            notificationService.notifyInappropriateUsername(user, newUsername);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brukernavnet inneholder upassende innhold");
        }

        if (!newUsername.equals(user.getUsername())
            && userRepository.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brukernavnet er allerede tatt");
        }

        user.setUsername(newUsername);
        userRepository.save(user);
        return getUserProfile(userId);
    }

}
