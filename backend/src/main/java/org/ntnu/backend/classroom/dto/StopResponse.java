package org.ntnu.backend.classroom.dto;

import lombok.Builder;
import lombok.Value;

/** Data Transfer Object for a teachers view of a stop on the map */
@Value
@Builder
public class StopResponse { 
    int id;
    String name;
    Integer displayOrder;
}
