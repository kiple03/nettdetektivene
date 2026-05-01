package org.ntnu.backend.avatar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a user's profile display settings.
 *
 * @param username  display name; must be 3–30 characters and non-blank
 * @param hairColor hex or named colour applied to the avatar's hair; may be null to keep existing
 */
public record UpdateUserProfileRequest(
    @NotBlank(message = "Brukernavn er påkrevd")
    @Size(min = 3, max = 30, message = "Brukernavn må være mellom 3 og 30 tegn")
    String username,
    String hairColor
) {}
