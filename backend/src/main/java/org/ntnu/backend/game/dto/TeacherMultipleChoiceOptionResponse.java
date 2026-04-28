package org.ntnu.backend.game.dto;

/** Full MC option data exposed to teachers (includes answer fields hidden from students). */
public record TeacherMultipleChoiceOptionResponse(
    int id,
    String text,
    String imageUrl,
    boolean isCorrect,
    String explanation
) implements TeacherOptionResponse {}
