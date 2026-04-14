package com.smartspend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for all REST controllers.
 * Returns clean JSON error responses instead of Spring's default HTML error page.
 *
 * <p>Scoped to /api/** REST controllers only. Thymeleaf page errors are handled separately.</p>
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.smartspend.controller",
                      annotations   = org.springframework.web.bind.annotation.RestController.class)
public class GlobalExceptionHandler {

    // ── 400 – Validation failures ─────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errorBody(
            HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors
        ));
    }

    // ── 401 – Unauthorized ────────────────────────────────────────────────

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleUnauthorized(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(
            HttpStatus.UNAUTHORIZED, ex.getMessage(), null
        ));
    }

    // ── 404 – Not Found ───────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(
            HttpStatus.NOT_FOUND, ex.getMessage(), null
        ));
    }

    // ── 409 – Conflict ────────────────────────────────────────────────────

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(
            HttpStatus.CONFLICT, ex.getMessage(), null
        ));
    }

    // ── 500 – Generic ─────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(errorBody(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null
        ));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Map<String, Object> errorBody(HttpStatus status, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        if (details != null) {
            body.put("details", details);
        }
        return body;
    }
}
