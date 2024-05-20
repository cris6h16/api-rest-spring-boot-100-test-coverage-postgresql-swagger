package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
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

import org.cris6h16.apirestspringboot.Constants.Cons.User;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.*;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Fails.*;

// Handles an exception in any annotated: @RestController, @Controller, or @RequestMapping
@RestControllerAdvice // global exception handler for RESTful controllers
@Slf4j
public class ExceptionHandlerControllers {
    ObjectMapper objectMapper;

    public ExceptionHandlerControllers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {
        String msg = ex.getMessage();
        String forClient = Cons.ExceptionHandler.defMsg.DataIntegrityViolation.UNHANDLED;

        boolean inUsername = thisContains(msg, "unique constraint", USERNAME_UNIQUE_NAME);
        boolean inEmail = thisContains(msg, "unique constraint", EMAIL_UNIQUE_NAME);
        boolean isHandledUniqueViolation = inUsername || inEmail;

        if (isHandledUniqueViolation) {
            forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;
            log.debug(forClient, msg);
            return buildResponse(HttpStatus.CONFLICT, forClient);
        }

        log.error(forClient, msg);
        return buildResponse(HttpStatus.BAD_REQUEST, forClient);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        String forClient = "ConstraintViolationException -> UNHANDLED {}";
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (!violations.isEmpty()) {
            forClient = violations.iterator().next().getMessage();
            log.debug("ConstraintViolationException: {}", forClient);
            return buildResponse(HttpStatus.BAD_REQUEST, forClient);
        }

        log.error(forClient, ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, forClient);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        String forClient = "IllegalArgumentException -> UNHANDLED {}";
        String msgL = ex.getMessage();
        if (thisContains(msgL, "for input string")) {
            log.debug("IllegalArgumentException: {}", Cons.Controller.Fails.Argument.DATATYPE_PASSED_WRONG);
            return buildResponse(HttpStatus.BAD_REQUEST, Cons.Controller.Fails.Argument.DATATYPE_PASSED_WRONG);
        }

        log.error("IllegalArgumentException -> UNHANDLED: {}", msgL);
        return buildResponse(HttpStatus.BAD_REQUEST, forClient);
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        String msg = ex.getMessage();
        String forClient = "Exception -> UNHANDLED {}";
        log.error(forClient, ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, forClient);
    }

    String getMapInJson(Map map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("ERROR PARSING TO JSON: {}", e.getMessage());
            return "ERROR PARSING TO JSON";
        }
    }

    public boolean thisContains(String msg, String... strings) {
        return Stream.of(strings).anyMatch(msg.toLowerCase()::contains);
    }

    private ResponseEntity<String> buildResponse(HttpStatus status, String message) {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", message);
        return ResponseEntity.status(status).body(getMapInJson(responseMap));
    }
}