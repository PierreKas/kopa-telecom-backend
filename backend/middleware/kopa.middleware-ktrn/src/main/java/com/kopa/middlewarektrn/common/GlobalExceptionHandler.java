package com.kopa.middlewarektrn.common;

import com.kopa.middlewarektrn.gateway.KtrnGatewayException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(KtrnGatewayException.class)
    public ResponseEntity<Map<String, Object>> handleGatewayFailure(KtrnGatewayException ex,
                                                                     HttpServletRequest request) {
        logger.error("KTRN gateway call failed", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(errorBody("KTRN_GATEWAY_ERROR", ex.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid request");
        return ResponseEntity.badRequest().body(errorBody("VALIDATION_ERROR", detail, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_ERROR", "Unexpected error", request));
    }

    private Map<String, Object> errorBody(String code, String message, HttpServletRequest request) {
        return Map.of(
                "code", code,
                "message", message,
                "path", request.getRequestURI(),
                "timestamp", Instant.now().toString()
        );
    }
}
