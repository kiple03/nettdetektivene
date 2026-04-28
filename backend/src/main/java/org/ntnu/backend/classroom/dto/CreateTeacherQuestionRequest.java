package org.ntnu.backend.classroom.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "levelType", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateMultipleChoiceQuestionRequest.class, name = "MULTIPLE_CHOICE"),
    @JsonSubTypes.Type(value = CreatePictureQuizQuestionRequest.class, name = "PICTURE_QUIZ"),
})
public interface CreateTeacherQuestionRequest {
    String questionText();
}
