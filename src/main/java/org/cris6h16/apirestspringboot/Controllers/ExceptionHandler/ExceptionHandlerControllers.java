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
    Map<String, String> map;
    String logMssg;

    public ExceptionHandlerControllers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.map = new HashMap<>(1);
        map.put("message", "Internal Server Error -> Unhandled");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {
        String msg = ex.getMessage().toLowerCase();
        String str, def;
        str = def = "Data Integrity Violation Exception -> UNHANDLED";

        // @UniqueConstraint were violated
        if (thisContains(msg, "unique constraint")) {
            if (thisContains(msg, "username_unique")) str = "Username already exists";
            else if (thisContains(msg, "email_unique")) str = "Email already exists";

            map.put("message", str);
            log.debug("DataIntegrityViolationException: {}", str);

            return ResponseEntity.status(HttpStatus.CONFLICT).body(getMapInJson());
        }

        // @NotBlank were ( null || doesn't contain at least one non-whitespace character )
        if (thisContains(msg, "null value in column", "email", "username", "password")) {
            str = "Email, Username and Password are Required";
            map.put("message", str);
            log.debug("DataIntegrityViolationException {}", str);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());
        }

        // Generic if wasn't handled
        log.error("DataIntegrityViolationException -> UNHANDLED: {}", msg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (!violations.isEmpty()) {
            String errorMessage = violations.iterator().next().getMessage();
            map.put("message", errorMessage);
            log.debug("ConstraintViolationException: {}", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());
        }

        log.error("ConstraintViolationException -> UNHANDLED: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        String msg = ex.getMessage().toLowerCase();
        String err;

        err = "Happened some error while we was passing the arguments";
        if (thisContains(msg, "cannot be null", "email", "username", "password")) {
            err = "Email, Username and Password are Required";
            map.put("message", err);
            log.debug("IllegalArgumentException: {}", err);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());
        }
        
        log.error("IllegalArgumentException -> UNHANDLED: {}", msg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        map.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(getMapInJson());
    }

    String getMapInJson() {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "ERROR PARSING TO JSON";
        }
    }

    public boolean thisContains(String msg, String... strings) {
        return Stream.of(strings).anyMatch(msg::contains);
    }
}