package org.ntnu.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.ntnu.backend.user.model.enums.RoleEnum;

/**
 * Request DTO for creation a new user.
 *
 * <p>Validated on arrival — all fields are required. The plain-text {@link #password}
 * is encoded by the service layer before persistence and is never stored or logged as-is.
 */
@Data
@Schema(description = "Request to create a new ")
public class CreateUserRequest {

  /**
   * The e-mail address for the new account. Must be a valid e-mail format and unique
   * across all existing users.
   */
  @NotBlank
  @Email
  @Schema(description = "User's email address", example = "john.doe@example.com")
  private String email;

  /**
   * The desired plain-text password. Must be between 8 and 255 characters.
   * BCrypt-encoded by the service before being stored — never persisted in plain text.
   */
  @NotBlank
  @Size(min = 8, max = 255)
  @Schema(description = "User's password")
  private String password;

  /**
   * The role to assign to the new account (e.g. {@code STUDENT} or {@code TEACHER}).
   * Determines which portal the user may access after registration.
   */
  @NotNull(message = "Role is required")
  @Schema(description = "Users role", example = "STUDENT")
  private RoleEnum role;
}
