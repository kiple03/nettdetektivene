package org.ntnu.backend.game.dto;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.game.model.enums.LevelType;

/**
 * Question as seen by a teacher: includes isDefault/classroomId and full option data
 * (answer fields included so the teacher can view and edit).
 * options is non-null only for MULTIPLE_CHOICE levels; empty list otherwise.
 */
public record TeacherQuestionResponse(
    int id,
    LevelType levelType,
    String questionText,
    List<TeacherMultipleChoiceOptionResponse> options,
    boolean isDefault,
    UUID classroomId
) {}
