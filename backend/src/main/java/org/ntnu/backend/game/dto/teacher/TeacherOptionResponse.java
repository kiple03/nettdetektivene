package org.ntnu.backend.game.dto.teacher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface for teacher-facing question option response DTOs.
 * Polymorphic JSON serialization is handled via the {@code type} field,
 * mapping to the concrete subtypes for each supported question kind.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TeacherMultipleChoiceOptionResponse.class, name = "MULTIPLE_CHOICE"),
    @JsonSubTypes.Type(value = TeacherPictureQuizOptionResponse.class, name = "PICTURE_QUIZ"),
    @JsonSubTypes.Type(value = TeacherClassifyItemsOptionResponse.class, name = "CLASSIFY_ITEMS"),
    @JsonSubTypes.Type(value = TeacherPasswordStrengthOptionResponse.class, name = "PASSWORD_STRENGTH"),
    @JsonSubTypes.Type(value = TeacherTextQuizOptionResponse.class, name = "TEXT_QUIZ"),
})
public interface TeacherOptionResponse {}
