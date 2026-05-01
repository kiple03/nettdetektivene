package org.ntnu.backend.game.dto;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.game.model.enums.LevelType;

/**
 * Question as seen by a teacher: includes isDefault/classroomId and full option data.
 * responseUrls type depends on levelType (polymorphic via TeacherOptionResponse).
 */
public record TeacherQuestionResponse(
    int id,
    LevelType levelType,
    String questionText,
    List<TeacherOptionResponse> options,
    boolean isDefault,
    UUID classroomId
) {}
