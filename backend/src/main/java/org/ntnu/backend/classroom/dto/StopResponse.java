package org.ntnu.backend.classroom.dto;

import lombok.Builder;
import lombok.Value;

/** Data Transfer Object for a teachers view of a stop on the map */
@Value
@Builder
public class StopResponse {
    /** Unique identifier of the stop. */
    int id;
    /** Display name of the stop shown to the teacher. */
    String name;
    /** 1-based position of this stop in the map sequence. */
    Integer displayOrder;
}
