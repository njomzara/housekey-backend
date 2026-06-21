package com.housekey.shared.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import com.housekey.auth.domain.InvalidCredentialsException;
import com.housekey.listings.domain.PropertyValidationException;
import com.housekey.media.domain.MediaValidationException;
import com.housekey.shared.i18n.LocalizedMessages;
import com.housekey.users.domain.DuplicateUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final LocalizedMessages messages;

    public GlobalExceptionHandler(LocalizedMessages messages) {
        this.messages = messages;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, message(ex), request, Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, message(ex), request, Map.of());
    }

    @ExceptionHandler(DuplicateUserException.class)
    ResponseEntity<ApiError> handleDuplicateUser(DuplicateUserException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, message(ex), request, messages.localizeFieldErrors(ex.getFieldErrors()));
    }

    @ExceptionHandler(PropertyValidationException.class)
    ResponseEntity<ApiError> handlePropertyValidation(PropertyValidationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, message(ex), request, messages.localizeFieldErrors(ex.getFieldErrors()));
    }

    @ExceptionHandler(MediaValidationException.class)
    ResponseEntity<ApiError> handleMediaValidation(MediaValidationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, message(ex), request, messages.localizeFieldErrors(ex.getFieldErrors()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, messages.get("error.media.maxUploadSize"), request, Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, messageOrDefault(ex, "error.accessDenied"), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), messages.get(fieldError));
        }
        return build(HttpStatus.BAD_REQUEST, messages.get("validation.request.failed"), request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                fieldErrors.put(violation.getPropertyPath().toString(), messages.getOrDefault(violation.getMessage())));
        return build(HttpStatus.BAD_REQUEST, messages.get("validation.request.failed"), request, fieldErrors);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, messageOrDefault(ex, "validation.request.failed"), request, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, messages.getOrDefault(ex.getMessage()), request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("error.serverUnexpected"), request, Map.of());
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors);
        return ResponseEntity.status(status).body(apiError);
    }

    private String message(LocalizedException ex) {
        return messages.get(ex.getMessageKey(), ex.getMessageArgs());
    }

    private String messageOrDefault(Exception ex, String defaultKey) {
        if (ex instanceof LocalizedException localized) {
            return message(localized);
        }
        String message = ex.getMessage();
        return message == null || message.isBlank() ? messages.get(defaultKey) : messages.getOrDefault(message);
    }
}
