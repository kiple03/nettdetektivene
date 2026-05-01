package org.ntnu.backend.classroom.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Polymorphic request interface for updating a teacher-created question.
 * Deserialized based on the {@code levelType} field into the appropriate subtype.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "levelType", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UpdateMultipleChoiceQuestionRequest.class, name = "MULTIPLE_CHOICE"),
    @JsonSubTypes.Type(value = UpdatePictureQuizQuestionRequest.class, name = "PICTURE_QUIZ"),
    @JsonSubTypes.Type(value = UpdateClassifyItemsQuestionRequest.class, name = "CLASSIFY_ITEMS"),
    @JsonSubTypes.Type(value = UpdatePasswordStrengthQuestionRequest.class, name = "PASSWORD_STRENGTH"),
    @JsonSubTypes.Type(value = UpdateTextQuizQuestionRequest.class, name = "TEXT_QUIZ"),
})
public interface UpdateTeacherQuestionRequest {
    /**
     * @return the updated question text shown to students
     */
    String questionText();
}
