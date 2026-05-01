package org.ntnu.backend.game.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.ntnu.backend.game.dto.PictureQuizOptionResponse;

/**
 * Internal model for a PICTURE_QUIZ option.
 *
 * <p>Mirrors {@link MultipleChoiceOption} but belongs to the PICTURE_QUIZ level type.
 * Each question has exactly one image (carried by {@code imageUrl}) and exactly one
 * correct category option. The three responseUrls on a question share the same image —
 * they differ only by their {@code id}, {@code text} (the category label shown to
 * the student), and {@code isCorrect}.
 *
 * <p>Stored as a JSON array in {@code Question.optionContent}. Example element:
 * <pre>{@code
 * {
 *   "id": 1,
 *   "text": "Ekte",
 *   "imageUrl": "https://example.com/photo.jpg",
 *   "imageAlt": "Beskrivelse av bildet for skjermleser",
 *   "isCorrect": false,
 *   "explanation": "Dette bildet er faktisk KI-generert."
 * }
 * }</pre>
 *
 * @see PictureQuizOptionResponse the stripped DTO returned to the client
 */
public record PictureQuizOption(

        @NotNull
        int id,

        String text,

        String imageUrl,

        String imageAlt,

        @NotNull
        boolean isCorrect,

        @NotBlank
        String explanation

) implements Option {

        public PictureQuizOption(
            int id,
            String text,
            String imageUrl,
            boolean isCorrect,
            String explanation
        ) {
                this(id, text, imageUrl, text, isCorrect, explanation);
        }
}