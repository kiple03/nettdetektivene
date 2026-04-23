package org.ntnu.backend.config.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.ntnu.backend.exception.ErrorResponse;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ApiResponse(content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public @interface ApiErrorResponse {
  String responseCode();
  String description();
}
