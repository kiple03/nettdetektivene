package org.ntnu.backend.game.dto;

/** PASSWORD_STRENGTH rule config for teachers. */
public record TeacherPasswordStrengthOptionResponse(
    int id,
    int minLength,
    boolean requireDigit,
    boolean requireUpper,
    boolean requireLower,
    boolean requireSymbol,
    String successFeedback,
    String failureFeedback
) implements TeacherOptionResponse {}
