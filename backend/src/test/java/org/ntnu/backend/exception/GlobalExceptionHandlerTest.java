package org.ntnu.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void errorResponseOf_shouldCreateGeneralErrorResponse() {
        ErrorResponse response = ErrorResponse.of("Something failed");

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Something failed");
        assertThat(response.field()).isNull();
    }

    @Test
    void errorResponseOf_withField_shouldCreateFieldErrorResponse() {
        ErrorResponse response = ErrorResponse.of("Invalid email", "email");

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Invalid email");
        assertThat(response.field()).isEqualTo("email");
    }

    @Test
    void handleAuthentication_shouldReturnUnauthorized() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = handler.handleAuthentication(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Bad credentials");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleValidationException_shouldReturnFirstFieldError() throws Exception {
        Method method = GlobalExceptionHandlerTest.class
                .getDeclaredMethod("dummyValidationMethod", String.class);

        MethodParameter methodParameter = new MethodParameter(method, 0);

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new DummyRequest(), "dummyRequest");

        bindingResult.addError(new FieldError(
                "dummyRequest",
                "email",
                "Email is required"
        ));

        bindingResult.addError(new FieldError(
                "dummyRequest",
                "username",
                "Username is required"
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Email is required");
        assertThat(response.getBody().field()).isEqualTo("email");
    }

    @Test
    void handleMissingCookie_shouldReturnBadRequestWithRefreshTokenMissingMessage() throws Exception {
        Method method = String.class.getDeclaredMethod("substring", int.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MissingRequestCookieException exception =
                new MissingRequestCookieException("refreshToken", methodParameter);

        ResponseEntity<ErrorResponse> response = handler.handleMissingCookie(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Refresh token missing");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequestWithExceptionMessage() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid level id");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Invalid level id");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleIllegalArgument_whenMessageIsNull_shouldReturnFallbackMessage() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Invalid request parameter");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleApi_shouldReturnStatusAndMessageFromApiException() {
        InvalidLevelSubmissionException exception =
                new InvalidLevelSubmissionException("Missing answer");

        ResponseEntity<ErrorResponse> response = handler.handleApi(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Missing answer");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleUserExists_shouldReturnConflictWithField() {
        UserAlreadyExistsException exception =
                new UserAlreadyExistsException("email", "test@example.com");

        ResponseEntity<ErrorResponse> response = handler.handleUserExists(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message())
                .isEqualTo("User already exists with email: test@example.com");
        assertThat(response.getBody().field()).isEqualTo("email");
    }

    @Test
    void handleResponseStatus_shouldPreserveStatusAndReason() {
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Classroom not found");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Classroom not found");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleResponseStatus_whenReasonIsNull_shouldUseStatusCodeAsMessage() {
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.NOT_FOUND);

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("404 NOT_FOUND");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        AccessDeniedException exception = new AccessDeniedException("No access");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Access denied");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void handleUnexpected_shouldReturnInternalServerErrorWithGenericMessage() {
        RuntimeException exception = new RuntimeException("Database exploded");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Something went wrong");
        assertThat(response.getBody().field()).isNull();
    }

    @Test
    void invalidLevelSubmissionException_shouldHaveBadRequestStatusAndMessage() {
        InvalidLevelSubmissionException exception =
                new InvalidLevelSubmissionException("Invalid submission");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Invalid submission");
    }

    @Test
    void invalidOptionFormatException_shouldHaveBadRequestStatusAndMessage() {
        InvalidOptionFormatException exception =
                new InvalidOptionFormatException("Invalid option format");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Invalid option format");
    }

    @Test
    void levelNotFoundException_shouldHaveNotFoundStatusAndMessage() {
        LevelNotFoundException exception = new LevelNotFoundException(12);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Level not found: 12");
    }

    @Test
    void optionValidationException_shouldHaveBadRequestStatusAndMessage() {
        OptionValidationException exception =
                new OptionValidationException("At least one option must be correct");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("At least one option must be correct");
    }

    @Test
    void questionLockedException_shouldHaveForbiddenStatusAndFixedMessage() {
        QuestionLockedException exception = new QuestionLockedException();

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getMessage())
                .isEqualTo("You must complete the previous question first");
    }

    @Test
    void questionNotFoundException_shouldHaveNotFoundStatusAndMessage() {
        QuestionNotFoundException exception = new QuestionNotFoundException(7);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Question not found: 7");
    }

    @Test
    void userAlreadyExistsException_shouldHaveConflictStatusMessageAndField() {
        UserAlreadyExistsException exception =
                new UserAlreadyExistsException("username", "Detective");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage())
                .isEqualTo("User already exists with username: Detective");
        assertThat(exception.getField()).isEqualTo("username");
    }

    @Test
    void userProgressNotFoundException_shouldHaveInternalServerErrorStatusAndMessage() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        UserProgressNotFoundException exception =
                new UserProgressNotFoundException(userId);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getMessage())
                .isEqualTo("User progress not found for user: " + userId);
    }

    @SuppressWarnings("unused")
    private void dummyValidationMethod(String value) {
        // Used only to create a MethodParameter for MethodArgumentNotValidException.
    }

    private static class DummyRequest {
    }
}