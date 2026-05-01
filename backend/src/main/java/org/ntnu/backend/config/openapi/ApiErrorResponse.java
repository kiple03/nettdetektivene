package org.ntnu.backend.config.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents a non-standard error response on a controller endpoint.
 *
 * <p>Used for status codes that are endpoint-specific (404, 409, etc.) and
 * not covered by the global 400/401/500 responses added in
 * {@link OpenApiConfig#globalResponseCustomizer()}.
 *
 * <p>Stack multiple on a single method for endpoints with several error paths.
 * The response body schema is always {@link org.ntnu.backend.exception.ErrorResponse}.
 *
 * <pre>{@code
 * @PostMapping("/register")
 * @ApiErrorResponse(responseCode = "409", description = "Email already in use")
 * public LoginResponse register(...) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ApiErrorResponses.class)
public @interface ApiErrorResponse {
  /** HTTP status code string, e.g. {@code "404"}. */
  String responseCode();

  /** Human-readable description shown in the Swagger UI for this error case. */
  String description();
}

