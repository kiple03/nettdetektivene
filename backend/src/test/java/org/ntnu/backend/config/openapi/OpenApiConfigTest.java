package org.ntnu.backend.config.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    void apiInfo_returnsOpenApiWithTitle() {
        OpenAPI api = openApiConfig.apiInfo();

        assertThat(api).isNotNull();
        assertThat(api.getInfo().getTitle()).isEqualTo("Nettdetektivene API");
        assertThat(api.getInfo().getVersion()).isEqualTo("1.0");
    }

    @Test
    void apiInfo_containsBearerAuthSecurityScheme() {
        OpenAPI api = openApiConfig.apiInfo();

        assertThat(api.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(api.getComponents().getSecuritySchemes().get("bearerAuth").getScheme())
                .isEqualTo("bearer");
    }

    @Test
    void apiInfo_containsErrorResponseSchema() {
        OpenAPI api = openApiConfig.apiInfo();

        assertThat(api.getComponents().getSchemas()).containsKey("ErrorResponse");
    }

    @Test
    void apiInfo_hasSecurityRequirement() {
        OpenAPI api = openApiConfig.apiInfo();

        assertThat(api.getSecurity()).isNotEmpty();
        assertThat(api.getSecurity().get(0).containsKey("bearerAuth")).isTrue();
    }

    @Test
    void globalResponseCustomizer_adds401ToAuthenticatedEndpoint() throws Exception {
        OperationCustomizer customizer = openApiConfig.globalResponseCustomizer();

        Operation operation = new Operation();
        operation.setResponses(new ApiResponses());

        HandlerMethod handlerMethod = mockHandlerMethod(SampleController.class, "securedEndpoint");

        customizer.customize(operation, handlerMethod);

        assertThat(operation.getResponses()).containsKey("401");
        assertThat(operation.getResponses().get("401").getDescription())
                .contains("Authentication required");
    }

    @Test
    void globalResponseCustomizer_skips401ForPublicEndpoint() throws Exception {
        OperationCustomizer customizer = openApiConfig.globalResponseCustomizer();

        Operation operation = new Operation();
        operation.setResponses(new ApiResponses());

        HandlerMethod handlerMethod = mockHandlerMethod(SampleController.class, "publicEndpoint");

        customizer.customize(operation, handlerMethod);

        assertThat(operation.getResponses()).doesNotContainKey("401");
    }

    @Test
    void globalResponseCustomizer_addsCustomErrorResponses() throws Exception {
        OperationCustomizer customizer = openApiConfig.globalResponseCustomizer();

        Operation operation = new Operation();
        operation.setResponses(new ApiResponses());

        HandlerMethod handlerMethod = mockHandlerMethod(SampleController.class, "endpointWith409");

        customizer.customize(operation, handlerMethod);

        assertThat(operation.getResponses()).containsKey("409");
        assertThat(operation.getResponses().get("409").getDescription())
                .isEqualTo("Already exists");
    }

    // ---- helpers ----

    private HandlerMethod mockHandlerMethod(Class<?> clazz, String methodName) throws Exception {
        Method method = clazz.getMethod(methodName);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        when(handlerMethod.getMethod()).thenReturn(method);
        when(handlerMethod.hasMethodAnnotation(PublicEndpoint.class))
                .thenReturn(method.isAnnotationPresent(PublicEndpoint.class));
        return handlerMethod;
    }

    // ---- sample controller used as test fixture ----

    static class SampleController {
        public void securedEndpoint() {}

        @PublicEndpoint
        public void publicEndpoint() {}

        @ApiErrorResponse(responseCode = "409", description = "Already exists")
        public void endpointWith409() {}
    }
}