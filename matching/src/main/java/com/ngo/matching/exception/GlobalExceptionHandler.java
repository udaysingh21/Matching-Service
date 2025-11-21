package com.ngo.matching.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyRegistered(AlreadyRegisteredException ex) {
        log.error(" Already registered: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Already Registered",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(NoSlotsAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleNoSlotsAvailable(NoSlotsAvailableException ex) {
        log.error(" No slots available: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "No Slots Available",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(NotRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handleNotRegistered(NotRegisteredException ex) {
        log.error(" Not registered: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Not Registered",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler({VolunteerNotFoundException.class, PostingNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        log.error(" Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", 404,
                "error", "Not Found",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<Map<String, Object>> handleServiceCommunication(ServiceCommunicationException ex) {
        log.error(" Service communication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", 503,
                "error", "Service Unavailable",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error(" Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", "An unexpected error occurred",
                "timestamp", LocalDateTime.now()
        ));
    }
}
