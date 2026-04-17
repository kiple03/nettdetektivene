package org.ntnu.backend.user.mapper;

import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.model.User;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {

  public LoginResponse toLoginResponse(String accessToken, String refreshToken) {
    return new LoginResponse(accessToken, refreshToken);
  }

  public User toUser(CreateUserRequest request, String encodedPassword) {
    return User.builder()
        .email(request.getEmail())
        .passwordHash(encodedPassword)
        .role(request.getRole())
        .build();
  }
}
