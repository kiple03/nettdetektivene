package org.ntnu.backend.avatar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.dto.UpdateUserProfileRequest;
import org.ntnu.backend.avatar.dto.UserProfileResponse;
import org.ntnu.backend.security.JwtService;
import org.ntnu.backend.security.UserPrincipal;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
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
    private final ClassroomCensorWordRepository classroomCensorWordRepository;
    private final JwtService jwtService;

    private String imageUrl(AvatarItem item) {
        String file = item.getImageFile();
        return "/assets/" + (file != null ? file : item.getName() + ".svg");
    }

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
                                            .imageUrl(imageUrl(inventoryItem.getAvatarItem()))
                                            .build())
                                        .toList();
        
        return UserProfileResponse.builder()
            .username(user.getUsername())
            .equippedItems(itemDtos)
            .hairColor(user.getHairColor())
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
     * Also appends locked items (items with an unlockCondition the user has not yet earned) so
     * the wardrobe grid can render them as blacked-out tiles.
     * @param userId user identification.
     * @return a list of all avatar items the user owns plus locked items not yet earned.
     */
    @Transactional
    public List<EquippedItemDto> getUserInventory(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        starterItems(user);
        List<UserInventoryItem> inventory = inventoryRepository.findByUserIdOrderByAvatarItemNameAsc(userId);

        Set<UUID> ownedIds = inventory.stream()
            .map(i -> i.getAvatarItem().getId())
            .collect(Collectors.toSet());

        List<EquippedItemDto> owned = inventory.stream()
            .map(item -> EquippedItemDto.builder()
                .id(item.getAvatarItem().getId())
                .name(item.getAvatarItem().getName())
                .category(item.getAvatarItem().getCategory())
                .imageUrl(imageUrl(item.getAvatarItem()))
                .locked(false)
                .build())
            .collect(Collectors.toList());

        List<EquippedItemDto> locked = avatarItemRepository.findByUnlockConditionNotNull().stream()
            .filter(item -> !ownedIds.contains(item.getId()))
            .map(item -> EquippedItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .imageUrl(imageUrl(item))
                .locked(true)
                .build())
            .toList();

        owned.addAll(locked);
        return owned;
    }

    /**
     * Grants all avatar items with the given unlock condition to the user, if not already owned.
     * Idempotent — calling it a second time does nothing.
     * @param userId user identification.
     * @param condition the unlock condition string (e.g. "BOSS_DEFEAT").
     */
    @Transactional
    public void unlockByCondition(UUID userId, String condition) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Set<UUID> ownedIds = inventoryRepository.findByUserId(userId).stream()
            .map(i -> i.getAvatarItem().getId())
            .collect(Collectors.toSet());

        List<UserInventoryItem> toGrant = avatarItemRepository.findByUnlockConditionNotNull().stream()
            .filter(item -> condition.equals(item.getUnlockCondition()) && !ownedIds.contains(item.getId()))
            .map(item -> UserInventoryItem.builder()
                .user(user)
                .avatarItem(item)
                .unlockedAt(LocalDateTime.now())
                .isEquipped(false)
                .build())
            .toList();

        if (!toGrant.isEmpty()) {
            inventoryRepository.saveAll(toGrant);
        }
    }

    private static final List<String> STARTER_ITEM_NAMES = List.of(
        "detective_hat", "detective_suit", "face_1", "face_2", "face_3",
        "magnifying_glass", "police_hat", "police_uniform", "sheriff_hat",
        "sheriff_star", "sheriff_suit", "stop_sign",
        "tone_light", "tone_medium", "tone_dark", "tone_wacky",
        "boy_hair", "girl_hair", "face_girl", "face_girl_2", "face_girl_3", "burning_hand",
        "afro", "girl_bob_hair", "mohawk", "armor", "stars"
    );

    private static final Set<String> DEFAULT_EQUIPPED = Set.of(
        "detective_hat", "detective_suit", "face_1", "tone_light", "boy_hair"
    );

    /**
     * Assigns any missing starter items to a user. Safe to call multiple times.
     * @param user the user instance.
     */
    @Transactional
    public void starterItems(User user){
        Set<String> existing = inventoryRepository.findByUserId(user.getId()).stream()
            .map(i -> i.getAvatarItem().getName())
            .collect(Collectors.toSet());

        List<AvatarItem> missing = avatarItemRepository.findAll().stream()
            .filter(item -> STARTER_ITEM_NAMES.contains(item.getName()) && !existing.contains(item.getName()))
            .toList();

        if (missing.isEmpty()) return;

        List<UserInventoryItem> toAdd = missing.stream().map(item ->
            UserInventoryItem.builder()
                .user(user)
                .avatarItem(item)
                .unlockedAt(LocalDateTime.now())
                .isEquipped(DEFAULT_EQUIPPED.contains(item.getName()))
                .build()
        ).toList();

        inventoryRepository.saveAll(toAdd);
    }

    /**
     * Updates the user's display name and hair colour, then issues a fresh JWT.
     * The new username is checked against the global profanity list and any classroom-specific
     * censor words. If it fails the check, a notification is sent and the request is rejected.
     *
     * @param userId      the ID of the user to update
     * @param classroomId the classroom whose censor-word list should be applied, or {@code null} to skip classroom-specific filtering
     * @param request     the requested new username and optional hair colour
     * @return a new JWT access token reflecting the updated profile
     * @throws org.springframework.web.server.ResponseStatusException 400 if the username contains profanity or censor words
     */
    @Transactional
    public String updateUserProfile(UUID userId, UUID classroomId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String newUsername = request.username().trim();

        List<String> classroomWords = classroomId == null ? List.of() :
            classroomCensorWordRepository.findByClassroomId(classroomId)
                .stream().map(w -> w.getWord()).toList();

        if (profanityFilter.containsProfanity(newUsername, classroomWords)) {
            notificationService.notifyInappropriateUsername(user, newUsername);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brukernavnet inneholder upassende innhold");
        }

        user.setUsername(newUsername);
        if (request.hairColor() != null) {
            user.setHairColor(request.hairColor());
        }
        userRepository.save(user);

        return jwtService.generateToken(new UserPrincipal(user), classroomId);
    }
}

