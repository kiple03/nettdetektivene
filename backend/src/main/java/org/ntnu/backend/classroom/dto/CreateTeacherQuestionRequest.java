package org.ntnu.backend.classroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.ntnu.backend.game.model.enums.LevelType;

public record CreateTeacherQuestionRequest(
    @NotBlank String questionText,
    @NotNull LevelType levelType,
    @NotEmpty List<@Valid CreateMultipleChoiceOptionRequest> options
) {}
