package org.ntnu.backend.game.dto;

import java.util.List;
import java.util.UUID;
import org.ntnu.backend.game.model.enums.LevelType;

/**
 * Question as seen by a teacher: includes ownership metadata and full option data.
 * The {@code options} list is polymorphic — its concrete type depends on {@code levelType}.
 *
 * @param id           unique question identifier
 * @param levelType    determines question mechanics and the concrete option type
 * @param questionText prompt shown to students
 * @param options      answer options with teacher-visible fields (e.g. correctness)
 * @param isDefault    {@code true} if this is a platform-default question (not classroom-owned)
 * @param classroomId  owning classroom; {@code null} when {@code isDefault} is {@code true}
 */
public record TeacherQuestionResponse(
    int id,
    LevelType levelType,
    String questionText,
    List<TeacherOptionResponse> options,
    boolean isDefault,
    UUID classroomId
) {}
