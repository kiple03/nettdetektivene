package org.ntnu.backend.game.dto;



public sealed interface OptionResponse permits MultipleChoiceOptionResponse,
    PasswordStrengthOptionResponse, PictureQuizOptionResponse, ClassifyItemsOptionResponse, PostOfficeOptionResponse {}