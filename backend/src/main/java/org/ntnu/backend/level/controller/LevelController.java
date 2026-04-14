package org.ntnu.backend.level.controller;

import jakarta.validation.Valid;
import org.ntnu.backend.level.dto.LevelResponse;
import org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;
import org.ntnu.backend.level.service.LevelService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/levels")
public class LevelController {

    private final LevelService levelService;

    public LevelController(LevelService levelService) {
        this.levelService = levelService;
    }

    @GetMapping("/{levelId}")
    public LevelResponse getLevel(@PathVariable int levelId) {
        return levelService.getLevel(levelId);
    }

    @PostMapping("/{levelId}/validate")
    public ValidateLevelAnswerResponse validateLevelAnswer(
            @PathVariable int levelId,
            @Valid @RequestBody ValidateLevelAnswerRequest request
    ) {
        return levelService.validateAnswer(levelId, request);
    }
}