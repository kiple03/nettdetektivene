package org.ntnu.backend.avatar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.avatar.dto.EquippedItemDto;
import org.ntnu.backend.avatar.dto.UpdateEquippedItemsRequest;
import org.ntnu.backend.avatar.dto.UpdateUserProfileRequest;
import org.ntnu.backend.avatar.dto.UserProfileResponse;
import org.ntnu.backend.avatar.service.UserAvatarService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AvatarControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserAvatarService userAvatarService;
    private JwtAuthenticatedPrincipal principal;

    private UUID userId;
    private UUID classroomId;

    @BeforeEach
    void setUp() {
        userAvatarService = mock(UserAvatarService.class);
        principal = mock(JwtAuthenticatedPrincipal.class);

        userId = UUID.randomUUID();
        classroomId = UUID.randomUUID();

        when(principal.getUserId()).thenReturn(userId);
        when(principal.getClassroomId()).thenReturn(classroomId);

        AvatarController controller = new AvatarController(userAvatarService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getUser_shouldReturnUserProfile() throws Exception {
        UUID itemId = UUID.randomUUID();

        UserProfileResponse response = UserProfileResponse.builder()
                .username("Sopika")
                .hairColor("#3b2417")
                .equippedItems(List.of(
                        EquippedItemDto.builder()
                                .id(itemId)
                                .name("detective_hat")
                                .category("hat")
                                .imageUrl("/assets/detective_hat.svg")
                                .build()
                ))
                .build();

        when(userAvatarService.getUserProfile(userId)).thenReturn(response);

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Sopika"))
                .andExpect(jsonPath("$.hairColor").value("#3b2417"))
                .andExpect(jsonPath("$.equippedItems[0].id").value(itemId.toString()))
                .andExpect(jsonPath("$.equippedItems[0].name").value("detective_hat"))
                .andExpect(jsonPath("$.equippedItems[0].category").value("hat"))
                .andExpect(jsonPath("$.equippedItems[0].imageUrl").value("/assets/detective_hat.svg"));

        verify(userAvatarService).getUserProfile(userId);
    }

    @Test
    void getInventory_shouldReturnUserInventory() throws Exception {
        UUID hatId = UUID.randomUUID();
        UUID suitId = UUID.randomUUID();

        List<EquippedItemDto> inventory = List.of(
                EquippedItemDto.builder()
                        .id(hatId)
                        .name("detective_hat")
                        .category("hat")
                        .imageUrl("/assets/detective_hat.svg")
                        .build(),
                EquippedItemDto.builder()
                        .id(suitId)
                        .name("detective_suit")
                        .category("outfit")
                        .imageUrl("/assets/detective_suit.svg")
                        .build()
        );

        when(userAvatarService.getUserInventory(userId)).thenReturn(inventory);

        mockMvc.perform(get("/user/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(hatId.toString()))
                .andExpect(jsonPath("$[0].name").value("detective_hat"))
                .andExpect(jsonPath("$[0].category").value("hat"))
                .andExpect(jsonPath("$[0].imageUrl").value("/assets/detective_hat.svg"))
                .andExpect(jsonPath("$[1].id").value(suitId.toString()))
                .andExpect(jsonPath("$[1].name").value("detective_suit"))
                .andExpect(jsonPath("$[1].category").value("outfit"))
                .andExpect(jsonPath("$[1].imageUrl").value("/assets/detective_suit.svg"));

        verify(userAvatarService).getUserInventory(userId);
    }

    @Test
    void saveOutfit_shouldUpdateEquippedItemsAndReturnOk() throws Exception {
        UUID itemOne = UUID.randomUUID();
        UUID itemTwo = UUID.randomUUID();

        UpdateEquippedItemsRequest request =
                new UpdateEquippedItemsRequest(List.of(itemOne, itemTwo));

        mockMvc.perform(put("/user/avatar/equip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userAvatarService).updateEquippedItems(userId, List.of(itemOne, itemTwo));
    }

    @Test
    void updateUserProfile_shouldUpdateProfileAndReturnUpdatedProfile() throws Exception {
        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest("NewName", "#111111");

        UserProfileResponse response = UserProfileResponse.builder()
                .username("NewName")
                .hairColor("#111111")
                .equippedItems(List.of())
                .build();

        when(userAvatarService.updateUserProfile(eq(userId), eq(classroomId), any(UpdateUserProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("NewName"))
                .andExpect(jsonPath("$.hairColor").value("#111111"))
                .andExpect(jsonPath("$.equippedItems").isArray());

        verify(userAvatarService).updateUserProfile(
                eq(userId),
                eq(classroomId),
                argThat(profileRequest ->
                        profileRequest.username().equals("NewName")
                                && profileRequest.hairColor().equals("#111111")
                )
        );
    }

    private HandlerMethodArgumentResolver authenticationPrincipalResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().equals(JwtAuthenticatedPrincipal.class);
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return principal;
            }
        };
    }
}