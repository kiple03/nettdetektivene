package org.ntnu.backend.game.dto;

/**
 * API response DTO for a single PICTURE_QUIZ option.
 *
 * <p>Intentionally omits {@code isCorrect} and {@code explanation} so that
 * answer data is never sent to the client before the student submits.
 *
 * <p>The {@code imageUrl} on each option points to the image the student
 * must classify. All three responseUrls on a question share the same URL —
 * only {@code id} and {@code text} (the category label) differ.
 */
public record PictureQuizOptionResponse(
        /** Unique identifier of the option. */
        int id,
        /** Category label the student selects as their answer. */
        String text,
        /** URL of the image the student must classify. */
        String imageUrl
) implements OptionResponse {}