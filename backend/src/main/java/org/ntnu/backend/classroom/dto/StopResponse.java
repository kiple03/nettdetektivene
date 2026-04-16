package org.ntnu.backend.classroom.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StopResponse {
    int id;
    String name;
    Integer displayOrder;
}
