package org.ntnu.backend.game.model;

/**
 * Sealed interface for options.
 * The root of the tagged union for options. Include all options that are permitted.
 */
public sealed interface Option
        permits MultipleChoiceOption, PictureQuizOption, PasswordStrengthOption, ClassifyItemsOption {
}