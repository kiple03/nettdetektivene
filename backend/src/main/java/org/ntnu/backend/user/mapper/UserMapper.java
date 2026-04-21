package org.ntnu.backend.user.mapper;

import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.model.User;
import org.springframework.stereotype.Component;

/**
 * Maps between user-related DTOs and domain objects.
 *
 * <p>Password encoding is intentionally handled by the caller before invoking
 * {@link #toUser}, keeping this class free of security dependencies and easy to test.
 */
@Component
public class UserMapper {

  /**
   * Assembles a {@link LoginResponse} from a JWT access token and a refresh token string.
   *
   * @param accessToken  the signed JWT to include in the response
   * @param refreshToken the opaque refresh token string to include in the response
   * @return a new {@link LoginResponse} containing both tokens
   */
  public LoginResponse toLoginResponse(String accessToken, String refreshToken) {
    return new LoginResponse(accessToken, refreshToken);
  }

  /**
   * Builds a new {@link User} entity from a registration request and a pre-encoded password.
   *
   * <p>The caller is responsible for encoding the password before passing it here;
   * this method stores the value as-is in {@link User#getPasswordHash()}.
   *
   * @param request         the validated registration payload containing e-mail and role
   * @param encodedPassword the BCrypt-encoded password hash to store on the entity
   * @return a new, unpersisted {@link User} ready to be saved via the repository
   */
  public User toUser(CreateUserRequest request, String encodedPassword) {
    return User.builder()
        .email(request.getEmail())
        .passwordHash(encodedPassword)
        .role(request.getRole())
        .build();
  }
}
