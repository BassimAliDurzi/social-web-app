package com.socialwebapp.api.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler {

    private static final String CODE = "validation_failed";
    private static final String MESSAGE = "Request validation failed.";

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<ValidationErrorItem> errors = ex.getAllErrors().stream()
                .map(this::mapResolvableError)
                .toList();

        return ResponseEntity.badRequest().body(new ValidationErrorResponse(CODE, MESSAGE, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationErrorItem> errors = ex.getConstraintViolations().stream()
                .map(this::mapConstraintViolation)
                .toList();

        return ResponseEntity.badRequest().body(new ValidationErrorResponse(CODE, MESSAGE, errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ValidationErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        return ResponseEntity.badRequest().body(new ValidationErrorResponse(CODE, MESSAGE, errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        List<ValidationErrorItem> errors = List.of(new ValidationErrorItem("request", "Invalid request."));
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(CODE, MESSAGE, errors));
    }

    private ValidationErrorItem mapConstraintViolation(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() == null ? "value" : v.getPropertyPath().toString();
        String msg = (v.getMessage() == null || v.getMessage().isBlank()) ? "Invalid value." : v.getMessage();
        return new ValidationErrorItem(field, msg);
    }

    private ValidationErrorItem mapFieldError(FieldError e) {
        String field = (e.getField() == null || e.getField().isBlank()) ? "field" : e.getField();
        String msg = (e.getDefaultMessage() == null || e.getDefaultMessage().isBlank())
                ? "Invalid value."
                : e.getDefaultMessage();
        return new ValidationErrorItem(field, msg);
    }

    private ValidationErrorItem mapResolvableError(MessageSourceResolvable e) {
        String msg = (e.getDefaultMessage() == null || e.getDefaultMessage().isBlank())
                ? "Invalid value."
                : e.getDefaultMessage();
        return new ValidationErrorItem("parameter", msg);
    }

    public record ValidationErrorResponse(String code, String message, List<ValidationErrorItem> errors) {
    }

    public record ValidationErrorItem(String field, String message) {
    }
}
