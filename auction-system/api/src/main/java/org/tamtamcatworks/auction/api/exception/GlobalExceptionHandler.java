package org.tamtamcatworks.auction.api.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(errorBody(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorBody(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    private Map<String, Object> errorBody(String message, HttpStatus status) {
        return Map.of(
            "timestamp", LocalDateTime.now(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", message
        );
    }
}
