package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// Handles an exception in any annotated: @RestController, @Controller, or @RequestMapping
@RestControllerAdvice // global exception handler for RESTful controllers
@Slf4j
public class ExceptionHandlerControllers { // TODO: correct HARD CODED
    ObjectMapper objectMapper;
    Map<String, String> map; // for client -> toJson(map)
    String msgL, forClient; // exception message lowercased, message for client

    public ExceptionHandlerControllers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.map = new HashMap<>(1);
        map.put("message", "Internal Server Error -> Unhandled");

    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {
        msgL = ex.getMessage().toLowerCase();

        // @UniqueConstraint were violated
        if (thisContains(msgL, "unique constraint")) {
            if (thisContains(msgL, "username_unique")) forClient = "Username already exists";
            else if (thisContains(msgL, "email_unique")) forClient = "Email already exists";

            map.put("message", forClient);
            log.debug("DataIntegrityViolationException: {}", forClient);

            return ResponseEntity.status(HttpStatus.CONFLICT).body(getMapInJson());
        }


        // Generic if wasn't handled
        log.error("DataIntegrityViolationException -> UNHANDLED: {}", msgL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (!violations.isEmpty()) {
            forClient = violations.iterator().next().getMessage();
            map.put("message", forClient);
            log.debug("ConstraintViolationException: {}", forClient);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());
        }

        log.error("ConstraintViolationException -> UNHANDLED: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        msgL = ex.getMessage().toLowerCase();

        log.error("IllegalArgumentException -> UNHANDLED: {}", msgL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        map.put("message", ex.getReason());
        log.debug("ResponseStatusException: {}", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(getMapInJson());
    }

    String getMapInJson() {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("ERROR PARSING TO JSON: {}", e.getMessage());
            return "ERROR PARSING TO JSON";
        }
    }

    public boolean thisContains(String msg, String... strings) {
        return Stream.of(strings).anyMatch(msg::contains);
    }
}