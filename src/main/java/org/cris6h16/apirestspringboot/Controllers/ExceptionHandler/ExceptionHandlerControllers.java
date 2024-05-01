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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("ConstraintViolationException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    //@ExceptionHandler(DuplicateKeyException.class)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {

        // constants violation
        String username_unique = "username_unique";
        String email_unique = "email_unique";
        String email_not_null = "null value in column \"email\"";
        String username_not_null = "null value in column \"username\"";
        String password_not_null = "null value in column \"password\"";

        if (ex.getMessage().contains(username_unique)) {
//            log.warn("DuplicateKeyException: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        if (ex.getMessage().contains(email_unique)) {
//            log.warn("DuplicateKeyException: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        boolean required = ex.getMessage().contains(email_not_null) ||
                ex.getMessage().contains(username_not_null) ||
                ex.getMessage().contains(password_not_null);
        if (required) {
//            log.warn("DataIntegrityViolationException: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("DataIntegrityViolationException");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKeyException(DuplicateKeyException ex) {
        // constants violation
        String username_unique = "username_unique";
        String email_unique = "email_unique";
        String email_not_null = "null value in column \"email\"";
        String username_not_null = "null value in column \"username\"";
        String password_not_null = "null value in column \"password\"";

        if (ex.getMessage().contains(username_unique)) {

//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
//        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
//        if (!violations.isEmpty()) {
//            String errorMessage = violations.iterator().next().getMessage();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
}

//    @ExceptionHandler(TransactionSystemException.class)
//    public ResponseEntity<String> handleTransactionSystemException(TransactionSystemException ex) {
//        // constants violation
//        String email_is_invalid = "Email is invalid";
//
//        if (ex.getMessage().contains(email_is_invalid)) {
//            log.warn("TransactionSystemException: {}", ex.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is invalid");
//        }
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
//    }

}
