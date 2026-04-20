package org.ntnu.backend.classroom.dto;

import lombok.Value;
import org.ntnu.backend.mystery.model.enums.MysteryStatusEnum;

/**
 * Data Transfer Object for status of mystery puzzle
 */
@Value
public class ReviewMysteryRequest {
    MysteryStatusEnum status;
    Boolean featured;
}
