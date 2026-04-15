package org.ntnu.backend.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClassroomResponse {
    private String id;
    private String title;
    private String description;
    private String joinCode;
}