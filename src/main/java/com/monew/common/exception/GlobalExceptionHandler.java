package com.monew.common.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MonewException.class)
    public ResponseEntity<ErrorResponse> handleMonewException(MonewException e) {
        log.warn("[{}] {}: {}", e.getErrorCode(), e.getClass().getSimpleName(), e.getDetails());
        ErrorResponse response = ErrorResponse.from(e);
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            ErrorCode.VALIDATION_FAILED.name(),
            ErrorCode.VALIDATION_FAILED.getMessage(),
            fieldErrors,
            e.getClass().getSimpleName(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            "METHOD_NOT_ALLOWED",
            e.getMessage(),
            Map.of(),
            e.getClass().getSimpleName(),
            HttpStatus.METHOD_NOT_ALLOWED.value()
        );
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다",
            Map.of(),
            e.getClass().getSimpleName(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }
}
