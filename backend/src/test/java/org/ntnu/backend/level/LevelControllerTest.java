package org.ntnu.backend.level.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;
import org.ntnu.backend.level.service.LevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LevelController.class)
class LevelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LevelService levelService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        LevelService levelService() {
            return mock(LevelService.class);
        }
    }

    @Test
    void validateLevelAnswer_returns200AndResponseBody() throws Exception {
        ValidateLevelAnswerRequest request = new ValidateLevelAnswerRequest();
        request.setOptionId("A");

        ValidateLevelAnswerResponse response =
                new ValidateLevelAnswerResponse(true, "Correct answer!", true);

        when(levelService.validateAnswer(anyInt(), any(ValidateLevelAnswerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/levels/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.message").value("Correct answer!"))
                .andExpect(jsonPath("$.unlockedNextLevel").value(true));
    }
}