package org.ntnu.backend.game.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.ntnu.backend.game.dto.subtypes.MultiSelectAnswer;
import org.ntnu.backend.game.dto.subtypes.PasswordStrengthAnswer;
import org.ntnu.backend.game.dto.subtypes.PerItemAnswer;
import org.ntnu.backend.game.dto.subtypes.SingleChoiceAnswer;

/**
 * Polymorphic base DTO for submitting an answer to a quiz question.
 * Deserialized via the {@code shape} discriminator field into one of the
 * concrete subtypes: {@code SINGLE}, {@code MULTI}, {@code PER_ITEM}, or {@code PASSWORD}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "shape")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SingleChoiceAnswer.class, name = "SINGLE"),
    @JsonSubTypes.Type(value = MultiSelectAnswer.class, name = "MULTI"),
    @JsonSubTypes.Type(value = PerItemAnswer.class, name = "PER_ITEM"),
    @JsonSubTypes.Type(value = PasswordStrengthAnswer.class, name = "PASSWORD")
})
public abstract class SubmitAnswerRequest { }
