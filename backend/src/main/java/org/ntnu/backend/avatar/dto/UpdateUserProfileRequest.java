package org.ntnu.backend.avatar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@NotBlank
@Size(min=3, max=20)
public record UpdateUserProfileRequest(
    String username
) {}