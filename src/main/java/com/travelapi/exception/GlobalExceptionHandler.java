package com.travelapi.exception;

import com.travelapi.patterns.LoggingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final LoggingService loggingService;

    public GlobalExceptionHandler(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        loggingService.error("ResourceNotFoundException", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.NOT_FOUND.value(), "Resource Not Found", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        loggingService.error("DuplicateResourceException", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.CONFLICT.value(), "Duplicate Resource", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Object> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
        loggingService.error("InvalidInputException", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST.value(), "Invalid Input", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<Object> handleDatabaseOperationException(DatabaseOperationException ex, WebRequest request) {
        loggingService.error("DatabaseOperationException", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Database Operation Failed", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        loggingService.error("IllegalArgumentException", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST.value(), "Invalid Argument", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        loggingService.error("IllegalStateException", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST.value(), "Illegal State", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        loggingService.error("Unhandled Exception", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An unexpected error occurred: " + ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorBody(int status, String error, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path.replace("uri=", ""));
        return body;
    }
}
