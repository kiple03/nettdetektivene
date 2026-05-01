package org.ntnu.backend.config.openapi;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.ntnu.backend.exception.ErrorResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * <p>Defines the API metadata, the JWT bearer security scheme, and a global
 * operation customizer that adds a 401 response to every endpoint without
 * requiring per-method annotations.
 *
 * <p>Swagger UI is available at {@code /swagger-ui/index.html}.
 * The raw OpenAPI spec is at {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";

  /**
   * Builds the top-level OpenAPI object with API metadata, JWT security scheme, and the
   * {@link ErrorResponse} schema registered for use in global error responses.
   *
   * @return configured {@link OpenAPI} instance
   */
  @Bean
  public OpenAPI apiInfo() {
    OpenAPI api = new OpenAPI()
        .info(new Info()
            .title("Nettdetektivene API")
            .description("Backend API for the Nettdetektivene learning game. "
                + "Most endpoints require a bearer JWT obtained via /auth/login.")
            .version("1.0"))
        .components(new Components()
            .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT bearer token from /auth/login")))
        .security(List.of(new SecurityRequirement().addList(BEARER_AUTH)));

      // Explicitly register the ErrorResponse schema so global error responses
      // can reference it via $ref. Normally springdoc only picks up schemas
      // from controller method signatures, and ErrorResponse is returned from
      // exception handlers only.
      ModelConverters.getInstance()
          .readAllAsResolvedSchema(ErrorResponse.class)
          .referencedSchemas
          .forEach((name, schema) -> api.getComponents().addSchemas(name, schema));

      return api;
  }

  /**
   * Adds a default 401 response to every operation. Individual endpoints can
   * still declare their own 401 with a more specific description if needed.
   *
   * @return an {@link OperationCustomizer} that injects standard error responses
   */
  @Bean
  public OperationCustomizer globalResponseCustomizer() {
    return (operation, handlerMethod) -> {
      Content errorContent = new Content()
          .addMediaType("application/json", new MediaType()
              .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")));

      if (!handlerMethod.hasMethodAnnotation(PublicEndpoint.class)) {
        operation.getResponses()
            .addApiResponse("401", new ApiResponse()
                .description("Authentication required or token invalid")
                .content(errorContent));
      }

      ApiErrorResponse[] customErrors = handlerMethod.getMethod()
          .getAnnotationsByType(ApiErrorResponse.class);
      for (ApiErrorResponse err : customErrors) {
        operation.getResponses()
            .addApiResponse(err.responseCode(), new ApiResponse()
                .description(err.description())
                .content(errorContent));
      }

      return operation;
    };
  }
}