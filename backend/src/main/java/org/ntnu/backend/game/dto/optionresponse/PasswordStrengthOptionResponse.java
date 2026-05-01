package org.ntnu.backend.game.dto.optionresponse;

/**
 * Response DTO representing a password-strength quiz option,
 * describing the rule set the player must satisfy to pass.
 *
 * @param id            unique identifier of the option
 * @param minLength     minimum required password length
 * @param requireDigit  whether at least one digit is required
 * @param requireUpper  whether at least one uppercase letter is required
 * @param requireLower  whether at least one lowercase letter is required
 * @param requireSymbol whether at least one special symbol is required
 * @param imageUrl      URL of the image displayed for this option
 */
public record PasswordStrengthOptionResponse (
    int id,
    int minLength,
    boolean requireDigit,
    boolean requireUpper,
    boolean requireLower,
    boolean requireSymbol,
    String imageUrl
) implements OptionResponse {
}
 