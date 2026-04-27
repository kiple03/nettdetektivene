package org.ntnu.backend.game.dto;

public sealed interface OptionResponse permits MultipleChoiceOptionResponse,
    PostOfficeOptionResponse, PasswordStrengthOptionResponse {}