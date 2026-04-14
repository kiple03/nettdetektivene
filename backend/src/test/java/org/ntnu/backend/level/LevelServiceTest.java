package org.ntnu.backend.level.service;

import org.junit.jupiter.api.Test;
import org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;

import static org.junit.jupiter.api.Assertions.*;

class LevelServiceTest {

    private final LevelService levelService = new LevelService();

    @Test
    void validateAnswer_returnsCorrectResponse_whenAnswerIsCorrect() {
        ValidateLevelAnswerRequest request = new ValidateLevelAnswerRequest();
        request.setOptionId("A");

        ValidateLevelAnswerResponse response = levelService.validateAnswer(1, request);

        assertTrue(response.isCorrect());
        assertEquals("Correct answer!", response.getMessage());
        assertTrue(response.isUnlockedNextLevel());
    }

    @Test
    void validateAnswer_returnsWrongResponse_whenAnswerIsIncorrect() {
        ValidateLevelAnswerRequest request = new ValidateLevelAnswerRequest();
        request.setOptionId("B");

        ValidateLevelAnswerResponse response = levelService.validateAnswer(1, request);

        assertFalse(response.isCorrect());
        assertEquals("Wrong answer, try again.", response.getMessage());
        assertFalse(response.isUnlockedNextLevel());
    }

    @Test
    void validateAnswer_returnsWrongResponse_whenLevelDoesNotExist() {
        ValidateLevelAnswerRequest request = new ValidateLevelAnswerRequest();
        request.setOptionId("A");

        ValidateLevelAnswerResponse response = levelService.validateAnswer(99, request);

        assertFalse(response.isCorrect());
        assertEquals("Wrong answer, try again.", response.getMessage());
        assertFalse(response.isUnlockedNextLevel());
    }

    @Test
    void validateLevelAnswer_returns400_whenOptionIdIsBlank() throws Exception {
        String requestBody = """
        {
          "optionId": ""
        }
        """;

        mockMvc.perform(post("/api/levels/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}