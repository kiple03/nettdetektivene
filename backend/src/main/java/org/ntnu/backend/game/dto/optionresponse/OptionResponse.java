package org.ntnu.backend.game.dto.optionresponse;

/**
 * Sealed marker interface for all game option response DTOs.
 * Each permitted subtype represents a different quiz/game option format
 * returned to the client.
 */
public sealed interface OptionResponse permits ClassifyItemsOptionResponse,
    MultipleChoiceOptionResponse, OptionImageResponse,
    PasswordStrengthOptionResponse, PictureQuizOptionResponse, PostOfficeOptionResponse {}