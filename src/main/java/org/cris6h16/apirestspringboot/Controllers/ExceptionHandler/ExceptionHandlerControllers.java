package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // global exception handler for REST controllers
@Slf4j
public class ExceptionHandlerControllers {
    ObjectMapper objectMapper;

    public ExceptionHandlerControllers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // handle my traversals exceptions
    @ExceptionHandler(value = {AbstractExceptionWithStatus.class})
    public ResponseEntity<String> handleServiceExceptionWithStatus(AbstractExceptionWithStatus ex) {
        return buildResponse(ex.getRecommendedStatus(), ex.getMessage());
    }

    // handle generic exceptions
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("ERROR: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    String getMapInJson(Map map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("ERROR PARSING TO JSON: {}", e.getMessage());
            return "ERROR PARSING TO JSON";
        }
    }


    // todo : make custom response fail with a class
    private ResponseEntity<String> buildResponse(HttpStatus status, String message) {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", message);
        return ResponseEntity.status(status).body(getMapInJson(responseMap));
    }
}