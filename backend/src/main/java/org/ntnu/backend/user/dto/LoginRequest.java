package org.ntnu.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.ntnu.backend.user.model.enums.RoleEnum;


/**
 * Request DTO for email/password login.
 *
 * <p>{@code portalRole} declares which portal the request originates from (STUDENT or TEACHER).
 * The backend verifies the authenticated user's role matches this intent and rejects the
 * login with 403 if there is a mismatch, preventing cross-portal authentication.
 */
@Data
@Schema(description = "Request to log in with email and password")
public class LoginRequest {

  /**
   * The user's e-mail address, used as the primary login identifier.
   * Must be a valid e-mail format and no longer than 255 characters.
   */
  @NotBlank
  @Email
  @Size(max = 255)
  @Schema(description = "User's email address")
  private String email;

  /**
   * The user's plain-text password. Must be between 8 and 255 characters.
   * Never stored or logged — matched against the persisted BCrypt hash server-side.
   */
  @NotBlank
  @Size(min = 8, max = 255)
  @Schema(description = "User's password")
  private String password;

  /**
   * The portal this login request originates from. Must exactly match the role stored
   * on the account; a mismatch results in a {@code 403 Forbidden} response.
   */
  @NotNull(message = "portalRole is required")
  @Schema(description = "Portal the login originates from — must match the user's actual role",
      example = "STUDENT")
  private RoleEnum portalRole;
}