package org.ntnu.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for username/password login.
 */
@Data
@Schema(description = "Request to log in with username and password")
public class LoginRequest {

  @NotBlank
  @Size(min = 8, max = 255)
  @Schema(description = "The username")
  private String username;

  @NotBlank
  @Size(min = 8, max = 255)
  @Schema(description = "User's password")
  private String password;
}