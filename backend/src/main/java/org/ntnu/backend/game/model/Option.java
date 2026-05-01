package org.ntnu.backend.game.model;

/**
 * Sealed interface for responseUrls.
 * The root of the tagged union for responseUrls. Include all responseUrls that are permitted.
 */
public sealed interface Option
        permits MultipleChoiceOption, PictureQuizOption, PostOfficeOption, PasswordStrengthOption, ClassifyItemsOption {
}