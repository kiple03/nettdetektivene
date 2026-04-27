package org.ntnu.backend.game.dto;

public sealed interface OptionResponse permits ClassifyItemsOptionResponse,
    MultipleChoiceOptionResponse, PasswordStrengthOptionResponse {}