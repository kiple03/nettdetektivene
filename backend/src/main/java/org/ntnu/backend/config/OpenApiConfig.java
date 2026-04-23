package org.ntnu.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
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

  @Bean
  public OpenAPI apiInfo() {
    return new OpenAPI()
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
  }

  /**
   * Adds a default 401 response to every operation. Individual endpoints can
   * still declare their own 401 with a more specific description if needed.
   */
  @Bean
  public OperationCustomizer globalResponseCustomizer() {
    return (operation, handlerMethod) -> {
      if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class)) {
        return operation;
      }
      operation.getResponses()
          .addApiResponse("401", new ApiResponse()
              .description("Authentication required or token invalid"));
      return operation;
    };
  }
}