package org.ntnu.backend.classroom.dto.createquestion;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Polymorphic request interface for creating a teacher question.
 * Deserialized based on the {@code levelType} field, which maps to a concrete subtype.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "levelType", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateMultipleChoiceQuestionRequest.class, name = "MULTIPLE_CHOICE"),
    @JsonSubTypes.Type(value = CreatePictureQuizQuestionRequest.class, name = "PICTURE_QUIZ"),
    @JsonSubTypes.Type(value = CreateClassifyItemsQuestionRequest.class, name = "CLASSIFY_ITEMS"),
    @JsonSubTypes.Type(value = CreatePasswordStrengthQuestionRequest.class, name = "PASSWORD_STRENGTH"),
    @JsonSubTypes.Type(value = CreateTextQuizQuestionRequest.class, name = "TEXT_QUIZ"),
})
public interface CreateTeacherQuestionRequest {
    /** @return the question prompt shown to students */
    String questionText();
}
