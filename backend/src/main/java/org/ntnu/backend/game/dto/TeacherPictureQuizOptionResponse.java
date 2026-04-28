package org.ntnu.backend.game.dto;

/** Full PICTURE_QUIZ option data for teachers (adds isCorrect and explanation hidden from students). */
public record TeacherPictureQuizOptionResponse(
    int id,
    String text,
    String imageUrl,
    boolean isCorrect,
    String explanation
) implements TeacherOptionResponse {}
