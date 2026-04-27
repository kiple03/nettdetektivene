package org.ntnu.backend.game.dto;

public record PasswordStrengthOptionResponse (
    int id,
    int minLength,
    boolean requireDigit,
    boolean requireUpper,
    boolean requireLower,
    boolean requireSymbol
) implements OptionResponse {
}
 