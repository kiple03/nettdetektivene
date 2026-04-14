package java.org.ntnu.backend.levelcontroller;

import java.org.ntnu.backend.level.dto.ValidateLevelAnswerRequest;
import java.org.ntnu.backend.level.dto.ValidateLevelAnswerResponse;
import java.org.ntnu.backend.level.service.LevelService;
import org.springframework.web.bind.annotation.*;

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
             @RequestBody ValidateLevelAnswerRequest request)
    {
        return levelService.validateAnswer(levelId, request);
    }
}