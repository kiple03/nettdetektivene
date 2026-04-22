package org.ntnu.backend.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CoTeacherResponse {
    private String userId;
    private String username;
    private String email;
    private boolean owner;
}
