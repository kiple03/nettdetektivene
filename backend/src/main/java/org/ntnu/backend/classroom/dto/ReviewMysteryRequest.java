package org.ntnu.backend.classroom.dto;

import lombok.Value;
import org.ntnu.backend.mystery.model.enums.MysteryStatusEnum;

@Value
public class ReviewMysteryRequest {
    MysteryStatusEnum status;
    Boolean featured;
}
