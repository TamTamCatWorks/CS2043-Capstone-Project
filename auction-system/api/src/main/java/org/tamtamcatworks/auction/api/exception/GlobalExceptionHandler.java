package org.tamtamcatworks.auction.api.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorized(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(errorBody(ex.getMessage(), HttpStatus.UNAUTHORIZED));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(errorBody(ex.getMessage(), HttpStatus.CONFLICT));
  }

  private Map<String, Object> errorBody(String message, HttpStatus status) {
    return Map.of(
        "timestamp", LocalDateTime.now(),
        "status", status.value(),
        "error", status.getReasonPhrase(),
        "message", message);
  }
}
