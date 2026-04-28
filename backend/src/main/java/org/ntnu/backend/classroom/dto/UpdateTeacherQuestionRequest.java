package org.ntnu.backend.classroom.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "levelType", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UpdateMultipleChoiceQuestionRequest.class, name = "MULTIPLE_CHOICE"),
})
public interface UpdateTeacherQuestionRequest {
    String questionText();
}
