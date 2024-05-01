package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;

// Handles an exception in any annotated: @RestController, @Controller, or @RequestMapping
@RestControllerAdvice // global exception handler for RESTful controllers
@Slf4j
public class ExceptionHandlerControllers {

//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
//        log.warn("ConstraintViolationException: {}", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
//    }

    //@ExceptionHandler(DuplicateKeyException.class)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {

        if (ex.getMessage().contains("unique constraint")) {
            String str = ex.getMessage().split("\"")[1]; // extract name of the unique constraint
            if (str.equals("username_unique")) str = "Username already exists";
            if (str.equals("email_unique")) str = "Email already exists";

            return ResponseEntity.status(HttpStatus.CONFLICT).body(str);
        }

        if (ex.getMessage().contains("null value in column") &&
                (ex.getMessage().contains("email") ||
                        ex.getMessage().contains("username") ||
                        ex.getMessage().contains("password"))) {
            String str = "Email, Username and Password are Required";

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(str);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data integrity violation");
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (!violations.isEmpty()) {
            String errorMessage = violations.iterator().next().getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Constraint violation");
    }
}
