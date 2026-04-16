package org.ntnu.backend.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassroomResponse {
    private String id;
    private String title;
    private String description;
    private String joinCode;
}