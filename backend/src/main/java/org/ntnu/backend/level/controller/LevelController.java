package org.ntnu.backend.levelcontroller;

import org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;
import org.ntnu.backend.level.service.LevelService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RequestController
@RequestMapping("/api/levels")
public class LevelController {
    private final LevelService levelService;

    public LevelController(LevelService levelService) {
        this.levelService = levelService;
    }

    @PostMapping("/{levelId}/validate")
    public ValidateLevelAnswerResponse validateAnswer(
             @PathVariable int levelId,
             @Valid @RequestBody ValidateLevelAnswerRequest request
    ) {
        return levelService.validateAnswer(levelId, request);
    }
}