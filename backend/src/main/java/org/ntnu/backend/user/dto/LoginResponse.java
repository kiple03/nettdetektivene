package org.ntnu.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO returned to the client after a successful login, registration, or token refresh.
 *
 * <p>The {@link #accessToken} is included in the JSON response body and should be attached
 * to subsequent API requests as a Bearer token. The {@link #refreshToken} is excluded from
 * the response body via {@link JsonIgnore} and must be delivered to the client through a
 * separate channel (e.g. an {@code HttpOnly} cookie) to reduce XSS exposure.
 */
@Data
@AllArgsConstructor
@Schema(description = "Response returned after successful login, registration, or token refresh")
public class LoginResponse {

  /**
   * Short-lived JWT used to authenticate API requests.
   * Attach as {@code Authorization: Bearer <accessToken>} on each request.
   */
  @Schema(description = "Short-lived JWT access token")
  private String accessToken;

  /**
   * Long-lived token used to obtain a new access token without re-entering credentials.
   * Excluded from the JSON response body — delivered via {@code HttpOnly} cookie instead.
   */
  @JsonIgnore
  private String refreshToken;
}
