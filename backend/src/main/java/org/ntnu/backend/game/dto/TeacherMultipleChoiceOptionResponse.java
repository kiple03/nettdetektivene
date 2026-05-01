package org.ntnu.backend.game.dto;

/**
 * Full MC option data exposed to teachers, including answer fields hidden from students.
 *
 * @param id          unique identifier of the option
 * @param text        display text of the option
 * @param imageUrl    optional image associated with the option
 * @param isCorrect   whether this option is the correct answer
 * @param explanation rationale shown after the question is answered
 */
public record TeacherMultipleChoiceOptionResponse(
    int id,
    String text,
    String imageUrl,
    boolean isCorrect,
    String explanation
) implements TeacherOptionResponse {}
