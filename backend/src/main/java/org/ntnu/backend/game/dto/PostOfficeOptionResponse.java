package org.ntnu.backend.game.dto;

public record PostOfficeOptionResponse(
    String id,
    String type
) implements OptionResponse {}