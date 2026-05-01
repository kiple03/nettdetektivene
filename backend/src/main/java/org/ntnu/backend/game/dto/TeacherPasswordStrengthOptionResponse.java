package org.ntnu.backend.game.dto;

/**
 * Response DTO carrying the PASSWORD_STRENGTH game-option configuration for a teacher.
 *
 * @param id              unique identifier of this option rule
 * @param minLength       minimum required password length
 * @param requireDigit    whether at least one digit is required
 * @param requireUpper    whether at least one uppercase letter is required
 * @param requireLower    whether at least one lowercase letter is required
 * @param requireSymbol   whether at least one special character is required
 * @param successFeedback feedback message shown when the password meets all criteria
 * @param failureFeedback feedback message shown when the password fails one or more criteria
 */
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
