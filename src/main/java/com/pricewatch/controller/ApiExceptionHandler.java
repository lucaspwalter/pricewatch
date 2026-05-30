package com.pricewatch.controller;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handle(ResponseStatusException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(Map.of(
                        "timestamp", OffsetDateTime.now(),
                        "status", exception.getStatusCode().value(),
                        "error", exception.getReason()
                ));
    }
}
