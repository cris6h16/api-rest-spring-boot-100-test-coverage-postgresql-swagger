package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // global exception handler for REST controllers
@Slf4j
public class ExceptionHandlerControllers {

    // handle my traversals exceptions
    @ExceptionHandler(value = {AbstractExceptionWithStatus.class})
    public ResponseEntity<String> handleServiceExceptionWithStatus(AbstractExceptionWithStatus ex) {
        return buildResponse(ex.getRecommendedStatus(), ex.getMessage());
    }


    // when a resource is not found ( the typical 404 NOT FOUND )
    @ExceptionHandler(value = NoResourceFoundException.class)
    // added thanks to the logs (ERROR)  todo: doc about the importance of a right & relevant logging
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, Cons.Response.ForClient.NO_RESOURCE_FOUND);
    }

    @ExceptionHandler(value = AccessDeniedException.class) // added thanks to the logs (ERROR)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, Cons.Auth.Fails.ACCESS_DENIED);
    }

    // when is e.g. `api/users/1` but was passed `api/users/string`
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class) // added thanks to the logs (ERROR)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, Cons.Response.ForClient.GENERIC_ERROR);
    }

    // handle generic exceptions
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        log.error("Unhandled exception: {}", ex.toString());
        return buildResponse(status, Cons.Response.ForClient.GENERIC_ERROR);
    }

    String buildFailJsonBody(String message, HttpStatus status, long timestamp) {
        String pre_body = """
                {
                    "message": "%s",
                    "status": "%s",
                    "timestamp": "%s"
                }
                """.replace("\n", "").replace(" ", ""); // improve the format for the logs
        if (message == null) message = "";
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        // long primitive type is not nullable

        return String.format(pre_body, message, status.toString(), timestamp);
    }


    // todo : make custom response fail with a class
    private ResponseEntity<String> buildResponse(HttpStatus status, String message) {

        String body = buildFailJsonBody(message, status, System.currentTimeMillis());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(body, headers, status);
    }

}