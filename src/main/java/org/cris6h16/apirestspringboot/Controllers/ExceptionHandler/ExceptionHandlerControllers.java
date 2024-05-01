package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Controllers.Exceptions.AlreadyExistsException;
import org.cris6h16.apirestspringboot.Controllers.Exceptions.AttributesRequiredAreNullException;
import org.cris6h16.apirestspringboot.Controllers.Exceptions.UnhundledDataIntegrityViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

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

            throw new AlreadyExistsException(str);
        }

        if (ex.getMessage().contains("null value in column") &&
                (ex.getMessage().contains("email") ||
                        ex.getMessage().contains("username") ||
                        ex.getMessage().contains("password"))) {
            String str = "Email, Username and Password are Required";

            throw new AttributesRequiredAreNullException(str);
        }

        throw new UnhundledDataIntegrityViolationException();
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

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleException(Exception ex) {
//        System.out.println("Exception: " + ex.toString());
//        return null;
//    }
}
