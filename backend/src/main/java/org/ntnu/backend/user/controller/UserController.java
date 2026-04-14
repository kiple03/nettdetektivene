package org.ntnu.backend.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Register, login, refresh token, and logout")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

  private final UserService userService;

  @Value("${jwt.refresh-expiration-ms}")
  private long refreshExpiration;

  @Operation(summary = "Register a new user", description = "Creates a new user. Returns access "
      + "token and refresh token in HttpOnly cookie.")
  @ApiResponse(responseCode = "200", description = "Registration successful")
  @ApiResponse(responseCode = "409", description = "Email already in use")
  @PostMapping("/register")
  public ResponseEntity<LoginResponse> register(@RequestBody @Valid CreateUserRequest request) {
    LoginResponse response = userService.register(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie(response.getRefreshToken()))
        .body(response);
  }

  @Operation(summary = "Login with username and password")
  @ApiResponse(responseCode = "200", description = "Login successful")
  @ApiResponse(responseCode = "401", description = "Login failed")
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    LoginResponse response = userService.login(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie(response.getRefreshToken()))
        .body(response);
  }

  // TODO: Should invalidate token
  @Operation(summary = "Logs the user out")
  @ApiResponse(responseCode = "200", description = "Successful logout")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    ResponseCookie cookie = ResponseCookie.from("refreshToken", null)
        .httpOnly(true)
        .secure(true)
        .path("/api/auth/refresh")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .build();
  }

  private String createRefreshCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path("/api/auth/refresh")
        .maxAge(refreshExpiration)
        .build()
        .toString();
  }
}
