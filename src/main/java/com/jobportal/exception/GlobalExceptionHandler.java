package com.jobportal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jobportal.dto.ResponseDTO;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import com.jobportal.dto.ErrorDTO;

/**
 * Centralized exception handler for the entire application.
 * Prevents stack traces from leaking to clients and returns clean JSON errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private Environment environment;

    /**
     * Handle all known business logic exceptions.
     * Returns 400 Bad Request with the exception message.
     */
    @ExceptionHandler(JobPortalException.class)
    public ResponseEntity<ErrorDTO> handleJobPortalException(JobPortalException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        String translatedMessage = environment.getProperty(ex.getMessage(), ex.getMessage());
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage().contains("UNAUTHORIZED") || ex.getMessage().contains("INVALID_CREDENTIALS")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex.getMessage().contains("NOT_FOUND")) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getMessage().contains("FORBIDDEN")) {
            status = HttpStatus.FORBIDDEN;
        }
        
        return new ResponseEntity<>(new ErrorDTO(translatedMessage), status);
    }

    /**
     * Handle Jakarta Bean Validation failures (@Valid annotation errors).
     * Returns 400 with a map of field → error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        log.warn("Validation errors: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all for any unexpected exceptions.
     * Returns 500 without exposing internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return new ResponseEntity<>(new ErrorDTO("An unexpected error occurred. Please try again."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
