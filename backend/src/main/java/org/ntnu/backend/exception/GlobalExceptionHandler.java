package org.ntnu.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centralised exception handler for all REST controllers.
 *
 * <p>Intercepts exceptions thrown anywhere in the controller layer and maps them to
 * consistent {@link ErrorResponse} bodies with appropriate HTTP status codes. The
 * handler priority is most-specific first: concrete exception types are handled before
 * the catch-all {@link Exception} handler at the bottom.
 *
 * <p>Handled exception hierarchy:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} → {@code 400} with the first field error</li>
 *   <li>{@link MissingRequestCookieException} → {@code 400} for a missing refresh-token cookie</li>
 *   <li>{@link IllegalArgumentException} → {@code 400} with the exception message</li>
 *   <li>{@link UserAlreadyExistsException} → status from exception with conflicting field included</li>
 *   <li>{@link ApiException} (and all other subclasses) → status from exception</li>
 *   <li>{@link Exception} (catch-all) → {@code 500} with a generic message</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Spring Security's {@link AuthenticationException} (and subclasses like
     * {@code BadCredentialsException}) thrown during login or other in-controller
     * authentication attempts. These indicate invalid credentials, not server errors.
     *
     * @param ex the authentication exception
     * @return {@code 401 Unauthorized} with the exception message
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handles Bean Validation failures ({@code @Valid} on request bodies or parameters).
     * Only the first field error is reported to keep the response concise.
     *
     * @param ex the validation exception containing one or more field errors
     * @return {@code 400 Bad Request} with the first failing field and its message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        String field = ex.getBindingResult().getFieldErrors().get(0).getField();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message, field));
    }

    /**
     * Handles a missing {@code refreshToken} cookie on token-refresh requests.
     *
     * @param ex the exception indicating which cookie was absent
     * @return {@code 400 Bad Request} with a generic missing-token message
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingCookie(MissingRequestCookieException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("Refresh token missing"));
    }

    /**
     * Handles invalid argument errors thrown by service or utility code.
     * Falls back to a generic message when the exception carries none.
     *
     * @param ex the exception describing the invalid argument
     * @return {@code 400 Bad Request} with the exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request parameter";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message));
    }

    /**
     * Handles all {@link ApiException} subclasses that do not have a more specific handler.
     * The HTTP status is taken directly from the exception, so each subclass controls
     * its own response code.
     *
     * @param ex the application-specific API exception
     * @return the status defined by the exception with a plain error message body
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handles duplicate-user conflicts during registration.
     * Unlike the generic {@link ApiException} handler, this includes the conflicting
     * {@code field} in the response so the client can highlight the correct form input.
     *
     * @param ex the exception identifying the conflicting field and value
     * @return {@code 409 Conflict} with both the message and the offending field name
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResponse.of(ex.getMessage(), ex.getField()));
    }

    /**
     * Handles {@link ResponseStatusException} thrown by service or controller code,
     * preserving the status and reason set on the exception. Without this handler
     * the catch-all below would rewrite every such exception to a generic 500.
     *
     * @param ex the exception carrying its intended HTTP status
     * @return a response with the exception's status and reason
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode()).body(ErrorResponse.of(message));
    }

    /**
     * Handles Spring Security's {@link AccessDeniedException} (and its subclass
     * {@code AuthorizationDeniedException} thrown by {@code @PreAuthorize}) so that
     * authorization failures surface as {@code 403} rather than being swallowed by
     * the catch-all handler below.
     *
     * @param ex the access-denied exception
     * @return {@code 403 Forbidden} with a generic message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("Access denied"));
    }

    /**
     * Catch-all handler for any unhandled exception. Logs no details to the client
     * to avoid leaking internal information.
     *
     * @param ex the unexpected exception
     * @return {@code 500 Internal Server Error} with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Something went wrong"));
    }
}