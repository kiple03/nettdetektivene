package org.ntnu.backend.config.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class AnnotationTest {

    // ---- PublicEndpoint ----

    @Test
    void publicEndpoint_isPresentOnAnnotatedMethod() throws Exception {
        Method method = AnnotatedMethods.class.getMethod("publicMethod");

        assertThat(method.isAnnotationPresent(PublicEndpoint.class)).isTrue();
    }

    @Test
    void publicEndpoint_isAbsentOnUnannotatedMethod() throws Exception {
        Method method = AnnotatedMethods.class.getMethod("securedMethod");

        assertThat(method.isAnnotationPresent(PublicEndpoint.class)).isFalse();
    }

    // ---- ApiErrorResponse ----

    @Test
    void apiErrorResponse_hasCorrectResponseCodeAndDescription() throws Exception {
        Method method = AnnotatedMethods.class.getMethod("methodWith404");
        ApiErrorResponse annotation = method.getAnnotation(ApiErrorResponse.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.responseCode()).isEqualTo("404");
        assertThat(annotation.description()).isEqualTo("Not found");
    }

    @Test
    void apiErrorResponse_isRepeatableViaContainer() throws Exception {
        Method method = AnnotatedMethods.class.getMethod("methodWithMultipleErrors");
        ApiErrorResponse[] annotations = method.getAnnotationsByType(ApiErrorResponse.class);

        assertThat(annotations).hasSize(2);
        assertThat(annotations[0].responseCode()).isEqualTo("404");
        assertThat(annotations[1].responseCode()).isEqualTo("409");
    }

    // ---- ApiErrorResponses container ----

    @Test
    void apiErrorResponses_containerHoldsMultipleAnnotations() throws Exception {
        Method method = AnnotatedMethods.class.getMethod("methodWithMultipleErrors");
        ApiErrorResponses container = method.getAnnotation(ApiErrorResponses.class);

        assertThat(container).isNotNull();
        assertThat(container.value()).hasSize(2);
    }

    // ---- test fixture ----

    static class AnnotatedMethods {
        @PublicEndpoint
        public void publicMethod() {}

        public void securedMethod() {}

        @ApiErrorResponse(responseCode = "404", description = "Not found")
        public void methodWith404() {}

        @ApiErrorResponse(responseCode = "404", description = "Not found")
        @ApiErrorResponse(responseCode = "409", description = "Conflict")
        public void methodWithMultipleErrors() {}
    }
}