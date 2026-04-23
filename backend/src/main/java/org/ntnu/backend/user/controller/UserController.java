package org.ntnu.backend.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ntnu.backend.config.openapi.PublicEndpoint;
import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication endpoints under {@code /auth}.
 *
 * <p>All endpoints that issue tokens return the JWT access token in the response body and
 * deliver the refresh token exclusively via an {@code HttpOnly} cookie named
 * {@code refreshToken}, scoped to {@code /api/auth/refresh}. This limits refresh-token
 * exposure to XSS attacks while keeping the access token accessible to JavaScript clients.
 *
 * <p>Cookie security (the {@code Secure} flag) is controlled by the
 * {@code app.cookie.secure} property, which should be {@code true} in production and may
 * be set to {@code false} in local development to allow plain HTTP.
 */
@Tag(name = "Authentication", description = "Register, login, refresh token, and logout")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

  private final UserService userService;

  /** Refresh-token lifetime in milliseconds, used to set the cookie {@code Max-Age}. */
  @Value("${jwt.refresh-expiration-ms}")
  private long refreshExpiration;

  /** Whether the {@code Secure} flag is applied to the refresh-token cookie. */
  @Value("${app.cookie.secure:true}")
  private boolean cookieSecure;

  /**
   * Registers a new user account and returns an authenticated session.
   *
   * <p>On success the access token is returned in the response body and the refresh token
   * is set as an {@code HttpOnly} cookie.
   *
   * @param request the validated registration payload
   * @return {@code 200 OK} with a {@link LoginResponse} body and a {@code Set-Cookie} header
   */
  @Operation(summary = "Register a new user", description = "Creates a new user. Returns access "
      + "token and refresh token in HttpOnly cookie.")
  @ApiResponse(responseCode = "200", description = "Registration successful")
  @ApiResponse(responseCode = "409", description = "Email already in use")
  @SecurityRequirements({})
  @PublicEndpoint
  @PostMapping("/register")
  public ResponseEntity<LoginResponse> register(@RequestBody @Valid CreateUserRequest request) {
    LoginResponse response = userService.register(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie(response.getRefreshToken()))
        .body(response);
  }

  /**
   * Authenticates a user with e-mail and password.
   *
   * <p>The request must specify the portal role that matches the account's role; a mismatch
   * results in {@code 403 Forbidden}. On success, tokens are delivered in the same way
   * as {@link #register}.
   *
   * @param request the validated login payload
   * @return {@code 200 OK} with a {@link LoginResponse} body and a {@code Set-Cookie} header
   */
  @Operation(summary = "Login with username and password")
  @ApiResponse(responseCode = "200", description = "Login successful")
  @ApiResponse(responseCode = "401", description = "Login failed")
  @PublicEndpoint
  @SecurityRequirements({})
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    LoginResponse response = userService.login(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie(response.getRefreshToken()))
        .body(response);
  }


  /**
   * Issues a new access token using the refresh token supplied via cookie.
   *
   * <p>The old refresh token is invalidated (one-time use) and a fresh one is written
   * back to the {@code HttpOnly} cookie.
   *
   * @param refreshToken the current refresh token, read from the {@code refreshToken} cookie
   * @return {@code 200 OK} with a new {@link LoginResponse} body and an updated cookie
   */
  @Operation(summary = "Refresh the access token")
  @ApiResponse(responseCode = "200", description = "New access token issued")
  @ApiResponse(responseCode = "400", description = "Missing access token cookie")
  @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(
      @CookieValue(name = "refreshToken") String refreshToken) {
    LoginResponse response = userService.refresh(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie(response.getRefreshToken()))
        .body(response);
  }

  /**
   * Logs the user out by revoking the refresh token and expiring the cookie.
   *
   * <p>If no {@code refreshToken} cookie is present the token-revocation step is skipped
   * and the response still clears the cookie, making this endpoint safe to call even when
   * the client is already logged out.
   *
   * @param refreshToken the refresh token to revoke, or {@code null} if the cookie is absent
   * @return {@code 200 OK} with an expired {@code Set-Cookie} header to clear the cookie
   */
  @Operation(summary = "Logs the user out")
  @ApiResponse(responseCode = "200", description = "Successful logout")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    if (refreshToken != null) {
      userService.logout(refreshToken);
    }

    ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/api/auth/refresh")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
        .build();
  }

  /**
   * Builds an {@code HttpOnly} {@code refreshToken} cookie for the given token value.
   *
   * <p>The cookie is scoped to {@code /api/auth/refresh} so it is only sent on refresh
   * requests, minimising the attack surface. Its {@code Max-Age} is derived from the
   * configured {@code jwt.refresh-expiration-ms} value (converted to seconds).
   *
   * @param refreshToken the refresh token string to embed in the cookie
   * @return the serialised {@code Set-Cookie} header value
   */
  private String createRefreshCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .path("/api/auth/refresh")
        .maxAge(refreshExpiration / 1000)
        .build()
        .toString();
  }
}
