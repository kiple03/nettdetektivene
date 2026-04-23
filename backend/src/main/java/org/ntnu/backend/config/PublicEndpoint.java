package org.ntnu.backend.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller endpoint as public (no authentication required).
 *
 * <p>Used by {@link OpenApiConfig#globalResponseCustomizer()} to skip
 * adding the default 401 response to the endpoint's OpenAPI documentation.
 *
 * <p>This annotation only affects documentation, not actual security.
 * Real access control is handled by Spring Security's
 * {@link org.springframework.security.config.annotation.web.builders.HttpSecurity}
 * configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PublicEndpoint {
}