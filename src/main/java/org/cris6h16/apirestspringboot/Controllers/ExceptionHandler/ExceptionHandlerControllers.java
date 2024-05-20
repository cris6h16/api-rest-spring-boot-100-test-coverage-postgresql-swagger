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

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;

@RestControllerAdvice // global exception handler for REST controllers
@Slf4j
public class ExceptionHandlerControllers {
    ObjectMapper objectMapper;

    public ExceptionHandlerControllers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
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


    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        String forClient = Cons.ExceptionHandler.defMsg.ConstraintViolation.UNHANDLED;
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (!violations.isEmpty()) {
            forClient = violations.iterator().next().getMessage();
            log.debug("ConstraintViolationException: {}", forClient);
            return buildResponse(HttpStatus.BAD_REQUEST, forClient);
        }

        log.error(forClient, ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, forClient);
    }


    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        String forClient = Cons.ExceptionHandler.defMsg.IllegalArgumentException.UNHANDLED;
        String msg = ex.getMessage();

        if (ex instanceof NumberFormatException) {
            forClient = Cons.ExceptionHandler.defMsg.IllegalArgumentException.NUMBER_FORMAT;
            log.debug(forClient, msg);
            return buildResponse(HttpStatus.BAD_REQUEST, forClient);
        }

        log.error(forClient, msg);
        return buildResponse(HttpStatus.BAD_REQUEST, forClient);
    }


    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

//    @ExceptionHandler(Exception.class) // absorbs some exceptions

    String getMapInJson(Map map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("ERROR PARSING TO JSON: {}", e.getMessage());
            return "ERROR PARSING TO JSON";
        }
    }

    public boolean thisContains(String msg, String... strings) {
        boolean contains = true;
        for (String s : strings) {
            contains = contains && msg.contains(s);
        }
        return contains;
    }

    private ResponseEntity<String> buildResponse(HttpStatus status, String message) {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", message);
        return ResponseEntity.status(status).body(getMapInJson(responseMap));
    }
}