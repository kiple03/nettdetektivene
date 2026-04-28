package org.ntnu.backend.config.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.ntnu.backend.config.openapi.ApiErrorResponse;

/**
 * Container for multiple {@link ApiErrorResponse} annotations on a single method.
 * Not used directly, {@link ApiErrorResponse} is repeatable and Java uses this
 * container under the hood when more than one is declared.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiErrorResponses {
  ApiErrorResponse[] value();
}