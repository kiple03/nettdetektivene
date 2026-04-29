package org.ntnu.backend.avatar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.dto.UpdateUserProfileRequest;
import org.ntnu.backend.avatar.dto.UserProfileResponse;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.ntnu.backend.avatar.model.UserInventoryItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;
import org.ntnu.backend.avatar.repository.UserInventoryItemRepository;
import org.ntnu.backend.classroom.repository.ClassroomCensorWordRepository;
import org.ntnu.backend.classroom.service.NotificationService;
import org.ntnu.backend.user.model.User;
import org.ntnu.backend.user.repository.UserRepository;
import org.ntnu.backend.user.service.ProfanityFilterService;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {

    @Mock
    private UserInventoryItemRepository inventoryRepository;

    @Mock
    private AvatarItemRepository avatarItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfanityFilterService profanityFilter;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ClassroomCensorWordRepository classroomCensorWordRepository;

    @InjectMocks
    private UserAvatarService userAvatarService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .username("Fredrik")
                .email("fredrik@example.com")
                .hairColor("#3b2417")
                .build();
    }

    @Test
    void getUserProfile_shouldReturnUsernameHairColorAndEquippedItems() {
        AvatarItem hat = avatarItem(UUID.randomUUID(), "detective_hat", "hat", null);
        AvatarItem suit = avatarItem(UUID.randomUUID(), "detective_suit", "outfit", "custom-suit.png");

        UserInventoryItem equippedHat = inventoryItem(hat, true);
        UserInventoryItem equippedSuit = inventoryItem(suit, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByUserIdAndIsEquippedTrue(userId))
                .thenReturn(List.of(equippedHat, equippedSuit));

        UserProfileResponse response = userAvatarService.getUserProfile(userId);

        assertEquals("Fredrik", response.getUsername());
        assertEquals("#3b2417", response.getHairColor());
        assertEquals(2, response.getEquippedItems().size());

        assertEquals(hat.getId(), response.getEquippedItems().get(0).getId());
        assertEquals("detective_hat", response.getEquippedItems().get(0).getName());
        assertEquals("hat", response.getEquippedItems().get(0).getCategory());
        assertEquals("/assets/detective_hat.svg", response.getEquippedItems().get(0).getImageUrl());

        assertEquals(suit.getId(), response.getEquippedItems().get(1).getId());
        assertEquals("detective_suit", response.getEquippedItems().get(1).getName());
        assertEquals("outfit", response.getEquippedItems().get(1).getCategory());
        assertEquals("/assets/custom-suit.png", response.getEquippedItems().get(1).getImageUrl());

        verify(userRepository).findById(userId);
        verify(inventoryRepository).findByUserIdAndIsEquippedTrue(userId);
    }

    @Test
    void getUserProfile_whenUserDoesNotExist_shouldThrowRuntimeException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userAvatarService.getUserProfile(userId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(inventoryRepository, never()).findByUserIdAndIsEquippedTrue(any());
    }

    @Test
    void updateEquippedItems_shouldEquipOnlySelectedItems() {
        UUID selectedItemId = UUID.randomUUID();
        UUID unselectedItemId = UUID.randomUUID();

        UserInventoryItem selectedItem = inventoryItem(
                avatarItem(selectedItemId, "detective_hat", "hat", null),
                false
        );

        UserInventoryItem unselectedItem = inventoryItem(
                avatarItem(unselectedItemId, "police_hat", "hat", null),
                true
        );

        when(inventoryRepository.findByUserId(userId))
                .thenReturn(List.of(selectedItem, unselectedItem));

        userAvatarService.updateEquippedItems(userId, List.of(selectedItemId));

        assertTrue(selectedItem.isEquipped());
        assertFalse(unselectedItem.isEquipped());

        verify(inventoryRepository).findByUserId(userId);
        verify(inventoryRepository).saveAll(List.of(selectedItem, unselectedItem));
    }

    @Test
    void updateEquippedItems_whenItemIdsIsNull_shouldUnequipAllItems() {
        UserInventoryItem itemOne = inventoryItem(
                avatarItem(UUID.randomUUID(), "detective_hat", "hat", null),
                true
        );

        UserInventoryItem itemTwo = inventoryItem(
                avatarItem(UUID.randomUUID(), "detective_suit", "outfit", null),
                true
        );

        when(inventoryRepository.findByUserId(userId))
                .thenReturn(List.of(itemOne, itemTwo));

        userAvatarService.updateEquippedItems(userId, null);

        assertFalse(itemOne.isEquipped());
        assertFalse(itemTwo.isEquipped());

        verify(inventoryRepository).findByUserId(userId);
        verify(inventoryRepository).saveAll(List.of(itemOne, itemTwo));
    }

    @Test
    void getUserInventory_shouldCreateStarterItemsAndReturnInventoryDtos() {
        AvatarItem starterHat = avatarItem(UUID.randomUUID(), "detective_hat", "hat", null);
        AvatarItem starterSuit = avatarItem(UUID.randomUUID(), "detective_suit", "outfit", "detective-suit.png");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByUserId(userId)).thenReturn(List.of());
        when(avatarItemRepository.findAll()).thenReturn(List.of(starterHat, starterSuit));
        when(inventoryRepository.findByUserIdOrderByAvatarItemNameAsc(userId))
                .thenReturn(List.of(
                        inventoryItem(starterHat, true),
                        inventoryItem(starterSuit, true)
                ));

        List<EquippedItemDto> inventory = userAvatarService.getUserInventory(userId);

        assertEquals(2, inventory.size());
        assertEquals("detective_hat", inventory.get(0).getName());
        assertEquals("/assets/detective_hat.svg", inventory.get(0).getImageUrl());
        assertEquals("detective_suit", inventory.get(1).getName());
        assertEquals("/assets/detective-suit.png", inventory.get(1).getImageUrl());

        verify(userRepository).findById(userId);
        verify(inventoryRepository).saveAll(anyList());
        verify(inventoryRepository).findByUserIdOrderByAvatarItemNameAsc(userId);
    }

    @Test
    void getUserInventory_whenUserDoesNotExist_shouldThrowRuntimeException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userAvatarService.getUserInventory(userId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(inventoryRepository, never()).findByUserIdOrderByAvatarItemNameAsc(any());
    }

    @Test
    void starterItems_whenMissingStarterItems_shouldSaveOnlyMissingStarterItems() {
        AvatarItem existingHat = avatarItem(UUID.randomUUID(), "detective_hat", "hat", null);
        AvatarItem missingSuit = avatarItem(UUID.randomUUID(), "detective_suit", "outfit", null);
        AvatarItem nonStarterItem = avatarItem(UUID.randomUUID(), "golden_crown", "hat", null);

        when(inventoryRepository.findByUserId(userId))
                .thenReturn(List.of(inventoryItem(existingHat, true)));
        when(avatarItemRepository.findAll())
                .thenReturn(List.of(existingHat, missingSuit, nonStarterItem));

        userAvatarService.starterItems(user);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserInventoryItem>> captor = ArgumentCaptor.forClass(List.class);

        verify(inventoryRepository).saveAll(captor.capture());

        List<UserInventoryItem> savedItems = captor.getValue();

        assertEquals(1, savedItems.size());
        assertEquals("detective_suit", savedItems.get(0).getAvatarItem().getName());
        assertTrue(savedItems.get(0).isEquipped());
        assertEquals(user, savedItems.get(0).getUser());
        assertNotNull(savedItems.get(0).getUnlockedAt());
    }

    @Test
    void starterItems_whenNoMissingStarterItems_shouldNotSaveAnything() {
        AvatarItem existingHat = avatarItem(UUID.randomUUID(), "detective_hat", "hat", null);

        when(inventoryRepository.findByUserId(userId))
                .thenReturn(List.of(inventoryItem(existingHat, true)));
        when(avatarItemRepository.findAll())
                .thenReturn(List.of(existingHat));

        userAvatarService.starterItems(user);

        verify(inventoryRepository, never()).saveAll(anyList());
    }

    @Test
    void updateUserProfile_shouldTrimUsernameUpdateHairColorSaveUserAndReturnProfile() {
        AvatarItem hat = avatarItem(UUID.randomUUID(), "detective_hat", "hat", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profanityFilter.containsProfanity("NewName", List.of())).thenReturn(false);
        when(inventoryRepository.findByUserIdAndIsEquippedTrue(userId))
                .thenReturn(List.of(inventoryItem(hat, true)));

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest("  NewName  ", "#111111");

        UserProfileResponse response =
                userAvatarService.updateUserProfile(userId, null, request);

        assertEquals("NewName", response.getUsername());
        assertEquals("#111111", response.getHairColor());
        assertEquals(1, response.getEquippedItems().size());

        assertEquals("NewName", user.getUsername());
        assertEquals("#111111", user.getHairColor());

        verify(userRepository, times(2)).findById(userId);
        verify(userRepository).save(user);
        verify(profanityFilter).containsProfanity("NewName", List.of());
    }

    @Test
    void updateUserProfile_whenHairColorIsNull_shouldOnlyUpdateUsername() {
        user.setUsername("OldName");
        user.setHairColor("#old");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profanityFilter.containsProfanity("OnlyName", List.of())).thenReturn(false);
        when(inventoryRepository.findByUserIdAndIsEquippedTrue(userId)).thenReturn(List.of());

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest("OnlyName", null);

        UserProfileResponse response =
                userAvatarService.updateUserProfile(userId, null, request);

        assertEquals("OnlyName", response.getUsername());
        assertEquals("#old", response.getHairColor());

        assertEquals("OnlyName", user.getUsername());
        assertEquals("#old", user.getHairColor());

        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfile_whenUsernameContainsProfanity_shouldNotifyAndThrowBadRequest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profanityFilter.containsProfanity("BadName", List.of())).thenReturn(true);

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest("BadName", "#222222");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userAvatarService.updateUserProfile(userId, null, request)
        );

        assertEquals(400, exception.getStatusCode().value());

        verify(notificationService).notifyInappropriateUsername(user, "BadName");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserProfile_whenUserDoesNotExist_shouldThrowRuntimeException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest("NewName", "#111111");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userAvatarService.updateUserProfile(userId, null, request)
        );

        assertEquals("User not found", exception.getMessage());

        verify(profanityFilter, never()).containsProfanity(anyString(), anyList());
        verify(userRepository, never()).save(any());
        verify(notificationService, never()).notifyInappropriateUsername(any(), anyString());
    }

    private AvatarItem avatarItem(UUID id, String name, String category, String imageFile) {
        return AvatarItem.builder()
                .id(id)
                .name(name)
                .category(category)
                .imageFile(imageFile)
                .build();
    }

    private UserInventoryItem inventoryItem(AvatarItem avatarItem, boolean equipped) {
        return UserInventoryItem.builder()
                .id(UUID.randomUUID())
                .user(user)
                .avatarItem(avatarItem)
                .isEquipped(equipped)
                .build();
    }
}