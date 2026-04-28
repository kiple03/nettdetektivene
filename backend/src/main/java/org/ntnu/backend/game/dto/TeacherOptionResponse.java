package org.ntnu.backend.game.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TeacherMultipleChoiceOptionResponse.class, name = "MULTIPLE_CHOICE"),
    @JsonSubTypes.Type(value = TeacherPictureQuizOptionResponse.class, name = "PICTURE_QUIZ"),
    @JsonSubTypes.Type(value = TeacherClassifyItemsOptionResponse.class, name = "CLASSIFY_ITEMS"),
})
public interface TeacherOptionResponse {}
