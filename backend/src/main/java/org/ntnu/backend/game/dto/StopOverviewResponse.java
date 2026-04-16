package org.ntnu.backend.game.dto;

public record StopOverviewResponse(
    int id,
    String name,
    boolean locked,
    boolean medalEarned
) {}
