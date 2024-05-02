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

// Handles an exception in any annotated: @RestController, @Controller, or @RequestMapping
@RestControllerAdvice // global exception handler for RESTful controllers
@Slf4j
public class ExceptionHandlerControllers { // TODO: correct HARD CODED
    ObjectMapper objectMapper;
    Map<String, String> map;

    public ExceptionHandlerControllers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.map = new HashMap<>();
        map.put("message", "Internal Server Error");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {

        if (ex.getMessage().contains("unique constraint")) {
            String str = ex.getMessage().split("\"")[1]; // extract name of the unique constraint
            if (str.equals("username_unique")) str = "Username already exists";
            if (str.equals("email_unique")) str = "Email already exists";
            map.put("message", str);

            return ResponseEntity.status(HttpStatus.CONFLICT).body(getMapInJson());
        }

        if (ex.getMessage().contains("null value in column") &&
                (ex.getMessage().contains("email") ||
                        ex.getMessage().contains("username") ||
                        ex.getMessage().contains("password"))) {
            String str = "Email, Username and Password are Required";
            map.put("message", str);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (!violations.isEmpty()) {
            String errorMessage = violations.iterator().next().getMessage();

            map.put("message", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());

        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getMapInJson());
    }


    String getMapInJson() {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "ERROR PARSING TO JSON";
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        String mssgL = ex.getMessage().toLowerCase();

        if (mssgL.contains("cannot be null") &&
                (mssgL.contains("email") || mssgL.contains("username") || mssgL.contains("password"))) {
            map.put("message", "Email, Username and Password are Required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());
        }

        map.put("message", "Happened some error while we was passing the arguments");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getMapInJson());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        map.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(getMapInJson());
    }
}
