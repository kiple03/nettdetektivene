package org.ntnu.backend.user.mapper;

import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.model.UserModel;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {

  public LoginResponse toLoginResponse(String accessToken, String refreshToken) {
    return new LoginResponse(
        accessToken,
        refreshToken
    );
  }

  public UserModel toUserModel(CreateUserRequest request, String encodedPassword) {
    return UserModel.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(encodedPassword)
        .role(request.getRole())
        .build();
  }
}
