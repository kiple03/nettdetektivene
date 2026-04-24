package org.ntnu.backend.avatar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
    @NotBlank(message = "Brukernavn er påkrevd")
    @Size(min = 3, max = 20, message = "Brukernavn må være mellom 3 og 20 tegn")
    String username,
    String hairColor
) {}
