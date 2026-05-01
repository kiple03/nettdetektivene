package org.ntnu.backend.game.dto;

/**
 * Full PICTURE_QUIZ option data returned to teachers.
 * Extends the student view by exposing {@code isCorrect} and {@code explanation},
 * which are hidden from students during an active game.
 *
 * @param id          unique option identifier
 * @param text        display label shown alongside the image
 * @param imageUrl    URL of the option image
 * @param isCorrect   whether this option is the correct answer
 * @param explanation teacher-facing rationale for why this option is correct or incorrect
 */
public record TeacherPictureQuizOptionResponse(
    int id,
    String text,
    String imageUrl,
    boolean isCorrect,
    String explanation
) implements TeacherOptionResponse {}
