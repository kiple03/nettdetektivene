package org.ntnu.backend.classroom.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "levelType", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UpdateMultipleChoiceQuestionRequest.class, name = "MULTIPLE_CHOICE"),
    @JsonSubTypes.Type(value = UpdatePictureQuizQuestionRequest.class, name = "PICTURE_QUIZ"),
    @JsonSubTypes.Type(value = UpdateClassifyItemsQuestionRequest.class, name = "CLASSIFY_ITEMS"),
    @JsonSubTypes.Type(value = UpdatePasswordStrengthQuestionRequest.class, name = "PASSWORD_STRENGTH"),
})
public interface UpdateTeacherQuestionRequest {
    String questionText();
}
