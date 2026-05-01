package org.ntnu.backend.game.dto;

/**
 * Full MC option data exposed to teachers, including answer fields hidden from students.
 *
 * @param id               unique identifier of the option
 * @param text             display text of the option
 * @param imageUrl         optional image shown to the student while answering
 * @param responseImageUrl optional image shown after the student submits (e.g. annotated version)
 * @param isCorrect        whether this option is the correct answer
 * @param explanation      rationale shown after the question is answered
 */
public record TeacherMultipleChoiceOptionResponse(
    int id,
    String text,
    String imageUrl,
    String responseImageUrl,
    boolean isCorrect,
    String explanation
) implements TeacherOptionResponse {}
