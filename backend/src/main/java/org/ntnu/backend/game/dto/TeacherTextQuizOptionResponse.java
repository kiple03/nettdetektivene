package org.ntnu.backend.game.dto;

/** TEXT_QUIZ option data exposed to teachers (no imageUrl; exactly one correct). */
public record TeacherTextQuizOptionResponse(
    int id,
    String text,
    boolean isCorrect,
    String explanation
) implements TeacherOptionResponse {}
