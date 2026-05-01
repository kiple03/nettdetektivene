package org.ntnu.backend.game.dto;

/**
 * TEXT_QUIZ option data exposed to teachers (no imageUrl; exactly one correct).
 *
 * @param id          unique option identifier
 * @param text        the answer text shown to students
 * @param isCorrect   whether this is the correct answer
 * @param explanation feedback shown after the question is answered
 */
public record TeacherTextQuizOptionResponse(
    int id,
    String text,
    boolean isCorrect,
    String explanation
) implements TeacherOptionResponse {}
