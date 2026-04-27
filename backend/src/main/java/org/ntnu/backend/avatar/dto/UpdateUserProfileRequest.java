package org.ntnu.backend.avatar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
    @NotBlank(message = "Brukernavn er påkrevd")
    @Size(min = 3, max = 30, message = "Brukernavn må være mellom 3 og 30 tegn")
    String username,
    String hairColor
) {}
