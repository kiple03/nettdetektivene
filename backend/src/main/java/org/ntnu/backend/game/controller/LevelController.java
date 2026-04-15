package org.ntnu.backend.game.controller;

import jakarta.validation.Valid;
//import org.ntnu.backend.game.dto.LevelResponse;
//import org.ntnu.backend.game.dto.ValidateLevelAnswerRequest;
//import org.ntnu.backend.game.dto.ValidateLevelAnswerResponse;
import org.ntnu.backend.game.service.LevelService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/levels")
public class LevelController {

    private final LevelService levelService;

    public LevelController(LevelService levelService) {
        this.levelService = levelService;
    }

//    @GetMapping("/{levelId}")
//    public LevelResponse getLevel(@PathVariable int levelId) {
//        return levelService.getLevel(levelId);
//    }
//
//    @PostMapping("/{levelId}/validate")
//    public ValidateLevelAnswerResponse validateLevelAnswer(
//            @PathVariable int levelId,
//            @Valid @RequestBody ValidateLevelAnswerRequest request
//    ) {
//        return levelService.validateAnswer(levelId, request);
//    }

}