package org.ntnu.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.ntnu.backend.user.model.enums.RoleEnum;


/**
 * Request DTO for username/password login.
 *
 * <p>{@code portalRole} declares which portal the request originates from (STUDENT or TEACHER).
 * The backend verifies the authenticated user's role matches this intent and rejects the
 * login with 403 if there is a mismatch, preventing cross-portal authentication.
 */
@Data
@Schema(description = "Request to log in with username and password")
public class LoginRequest {

  @NotBlank
  @Size(min = 3, max = 255)
  @Schema(description = "The username")
  private String username;

  @NotBlank
  @Size(min = 8, max = 255)
  @Schema(description = "User's password")
  private String password;

  @NotNull(message = "portalRole is required")
  @Schema(description = "Portal the login originates from — must match the user's actual role",
      example = "STUDENT")
  private RoleEnum portalRole;
}