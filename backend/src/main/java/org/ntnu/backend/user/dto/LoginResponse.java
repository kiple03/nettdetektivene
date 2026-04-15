package org.ntnu.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response returned after successful login, registration, or token refresh")
public class LoginResponse {

  @Schema(description = "Short-lived JWT access token")
  private String accessToken;

  @JsonIgnore
  private String refreshToken;
}
