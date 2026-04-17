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
 */
@Data
@Schema(description = "Request to create a new ")
public class CreateUserRequest {

  @NotBlank
  @Email
  @Schema(description = "User's email address", example = "john.doe@example.com")
  private String email;

  @NotBlank
  @Size(min = 3, max = 255)
  @Schema(description = "The username")
  private String username;

  @NotBlank
  @Size(min = 8, max = 255)
  @Schema(description = "User's password")
  private String password;

  @NotNull(message = "Role is required")
  @Schema(description = "Users role", example = "STUDENT")
  private RoleEnum role;
}
