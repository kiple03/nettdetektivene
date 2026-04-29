package org.ntnu.backend.medalion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.medalion.dto.AwardMedalionRequest;
import org.ntnu.backend.medalion.dto.MedalionResponse;
import org.ntnu.backend.medalion.service.MedalionService;
import org.ntnu.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MedalionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MedalionService medalionService;

    private UUID userId;
    private UUID medalionId;
    private LocalDateTime earnedAt;

    private JwtAuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        medalionService = mock(MedalionService.class);
        principal = mock(JwtAuthenticatedPrincipal.class);

        MedalionController medalionController = new MedalionController(medalionService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(medalionController)
                .setValidator(validator)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(JwtAuthenticatedPrincipal.class);
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
                })
                .build();

        userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        medalionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        earnedAt = LocalDateTime.of(2026, 4, 28, 12, 30);
    }

    @Test
    void awardUser_shouldReturnAwardedMedalion() throws Exception {
        AwardMedalionRequest request = new AwardMedalionRequest();
        request.setUserId(userId);
        request.setName("Rekruttert detektiv");
        request.setDescription("Lagde bruker");

        MedalionResponse response = MedalionResponse.builder()
                .id(medalionId)
                .name("Rekruttert detektiv")
                .description("Lagde bruker")
                .earnedAt(earnedAt)
                .imageUrl("/assets/medalions/Rekruttert detektiv.svg")
                .build();

        when(medalionService.awardMedalion(any(AwardMedalionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/user/medalions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(medalionId.toString()))
                .andExpect(jsonPath("$.name").value("Rekruttert detektiv"))
                .andExpect(jsonPath("$.description").value("Lagde bruker"))
                .andExpect(jsonPath("$.earnedAt").value("2026-04-28T12:30:00"))
                .andExpect(jsonPath("$.imageUrl").value("/assets/medalions/Rekruttert detektiv.svg"));

        verify(medalionService).awardMedalion(any(AwardMedalionRequest.class));
    }

    @Test
    void awardUser_withInvalidRequest_shouldReturnBadRequestAndNotCallService() throws Exception {
        AwardMedalionRequest request = new AwardMedalionRequest();
        request.setUserId(null);
        request.setName("");
        request.setDescription("");

        mockMvc.perform(post("/user/medalions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(medalionService, never()).awardMedalion(any(AwardMedalionRequest.class));
    }

    @Test
    void getMedalions_shouldReturnMedalionsForAuthenticatedUser() throws Exception {
        when(principal.getUserId()).thenReturn(userId);

        MedalionResponse response = MedalionResponse.builder()
                .id(medalionId)
                .name("Rekruttert detektiv")
                .description("Lagde bruker")
                .earnedAt(earnedAt)
                .imageUrl("/assets/medalions/Rekruttert detektiv.svg")
                .build();

        when(medalionService.getAllUsersMedalion(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/user/medalions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(medalionId.toString()))
                .andExpect(jsonPath("$[0].name").value("Rekruttert detektiv"))
                .andExpect(jsonPath("$[0].description").value("Lagde bruker"))
                .andExpect(jsonPath("$[0].earnedAt").value("2026-04-28T12:30:00"))
                .andExpect(jsonPath("$[0].imageUrl").value("/assets/medalions/Rekruttert detektiv.svg"));

        verify(medalionService).getAllUsersMedalion(userId);
    }

    @Test
    void getMedalions_whenUserHasNoMedalions_shouldReturnEmptyList() throws Exception {
        when(principal.getUserId()).thenReturn(userId);

        when(medalionService.getAllUsersMedalion(userId)).thenReturn(List.of());

        mockMvc.perform(get("/user/medalions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(medalionService).getAllUsersMedalion(userId);
    }
}