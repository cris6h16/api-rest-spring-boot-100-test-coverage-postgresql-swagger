package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Utils.FilesSyncUtils;
import org.cris6h16.apirestspringboot.Utils.SychFor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;

/**
 * Handling of exception in the application and give a custom response
 * based on the exception type && its message
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerControllers {

    private final FilesSyncUtils filesSyncUtils;

    public ExceptionHandlerControllers(FilesSyncUtils filesSyncUtils) {
        this.filesSyncUtils = filesSyncUtils;
    }

    /**
     * Pass the message to the client if happened some {@link  ConstraintViolationException}
     * like {@link NotNull}, {@link  NotBlank}, {@link  Email}, etc
     *
     * @param e the {@link  ConstraintViolationException}
     * @return the response with the {@code validationFail.message} and status {@code BAD_REQUEST}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        logHandledDebug(e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String forClient = Cons.Response.ForClient.GENERIC_ERROR;

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        if (!violations.isEmpty()) forClient = violations.iterator().next().getMessage();

        return buildAFailResponse(status, forClient);
    }


    /**
     * If a {@link PropertyReferenceException} was thrown trying to sort
     * a page with a nonexistent attribute
     *
     * @param e the exception
     * @return the response with the proper message and status {@code BAD_REQUEST}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<String> handlePropertyReferenceException(PropertyReferenceException e) {
        logHandledDebug(e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String forClient = Cons.Response.ForClient.GENERIC_ERROR;

        // structure: No property '<ttt>' found for type '<UserEntity>'
        boolean propertyNonexistent = this.thisContains(e.getMessage(), "for type");
        if (propertyNonexistent)
            forClient = e.getMessage().split("for type")[0].trim(); // expose: No property '<ttt>' found

        return buildAFailResponse(status, forClient);
    }

    /**
     * Handles when a {@link DataIntegrityViolationException} is thrown
     * due to some constraint violation like unique constraints
     *
     * @param e the mentioned Exception
     * @return the response with the proper message && status {@code 409 Conflict}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logHandledDebug(e);
        HttpStatus status = HttpStatus.CONFLICT;
        String forClient = Cons.Response.ForClient.GENERIC_ERROR;

        // for the UserEntity
        boolean inUsername = thisContains(e.getMessage(), USERNAME_UNIQUE_NAME);
        boolean inEmail = thisContains(e.getMessage(), EMAIL_UNIQUE_NAME);
        boolean isHandledUniqueViolation = inUsername || inEmail;
        if (isHandledUniqueViolation) forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;

        return buildAFailResponse(status, forClient);
    }

    // added thanks to the logs in file (ERROR)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logHandledDebug(e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String forClient = Cons.Response.ForClient.GENERIC_ERROR;


        boolean isRBMissing = thisContains(e.getMessage(), "Request body", "missing");
        if (isRBMissing) forClient = Cons.Response.ForClient.REQUEST_BODY_MISSING;

        return buildAFailResponse(status, forClient);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logHandledDebug(e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String forClient = Cons.Response.ForClient.GENERIC_ERROR;

        List<ObjectError> msgs = e.getAllErrors();
        if (!msgs.isEmpty()) forClient = msgs.getFirst().getDefaultMessage();

        return buildAFailResponse(status, forClient);
    }

    /**
     * Handling of {@link NoResourceFoundException}
     *
     * @param ex exception
     * @return a proper response complying the principle of least privilege
     * @note Added thanks to {@link #logUnhandledException(Exception)}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
        logHandledDebug(ex);
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String msg = Cons.Auth.Fails.UNAUTHORIZED;

        if (isAuthenticated()) {
            status = HttpStatus.NOT_FOUND;
            msg = Cons.Response.ForClient.NO_RESOURCE_FOUND;
        }
        return buildAFailResponse(status, msg);
    }

    /**
     * Handling for {@link AccessDeniedException}
     *
     * @param ex the exception
     * @return A proper response complying the principle of least privilege
     * @note Added thanks to {@link #logUnhandledException(Exception)}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = AccessDeniedException.class) // added thanks to the logs (ERROR)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        logHandledDebug(ex);

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String msg = Cons.Auth.Fails.UNAUTHORIZED;

        if (isAuthenticated()) {
            if (!isAdmin()) {
                status = HttpStatus.NOT_FOUND;
                msg = Cons.Response.ForClient.NO_RESOURCE_FOUND;
            } /* else { --> for the moment all are permitted for the admin ( commented for reach the better coverage )
                status = HttpStatus.FORBIDDEN;
                msg = Cons.Auth.Fails.ACCESS_DENIED;
            }*/
        }

        return buildAFailResponse(status, msg);
    }

    /**
     * Handling of {@link MethodArgumentTypeMismatchException}
     * <p>
     * Example of when it can be thrown: <br>
     * <strong>ENDPOINT</strong> {@code GET api/users/1} but was {@code GET api/users/string}
     * </p>
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with status {@link HttpStatus#BAD_REQUEST}
     * and a generic message
     * @note Added thanks to {@link #logUnhandledException(Exception)}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        logHandledDebug(ex);
        return buildAFailResponse(HttpStatus.BAD_REQUEST, Cons.Response.ForClient.GENERIC_ERROR);
    }

    /**
     * Handling of {@link HttpMediaTypeNotSupportedException}
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with status with the proper status code
     * @note Added thanks to {@link #logUnhandledException(Exception)}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        logHandledDebug(ex);
        return buildAFailResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE);
    }


    /**
     * Handling of generic exceptions
     *
     * @param e the exception
     * @return a {@link ResponseEntity} with status {@link HttpStatus#INTERNAL_SERVER_ERROR}
     * and message {@link Cons.Response.ForClient#GENERIC_ERROR}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception e) {
        logUnhandledException(e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildAFailResponse(status, Cons.Response.ForClient.GENERIC_ERROR);
    }

    /**
     * Build a json body for a response of a failed request
     *
     * @param message   value for the {@code message} in the json body
     * @param status    value for the {@code status} in the json body
     * @param timestamp value for the {@code timestamp} in the json body
     * @return a json body with the given values
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private String buildFailJsonBody(String message, HttpStatus status, long timestamp) {
        String pre_body = """
                {
                    "message": "%s",
                    "status": "%s",
                    "timestamp": "%s"
                }
                """
                .replace("\n", "")
                .replace(" ", ""); // improve the format for the logs
        if (message == null) message = "";
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        // long primitive type is not nullable

        return String.format(pre_body, message, status.toString(), timestamp);
    }

    /**
     * Build a {@link ResponseEntity<String>} for a failed request
     *
     * @param status  the proper status related to the exception
     * @param message the message to be shown in the response
     * @return a {@link ResponseEntity<String>} with the given status and message ready for show to
     * the client, with {@link MediaType#APPLICATION_JSON} as content type
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private ResponseEntity<String> buildAFailResponse(HttpStatus status, String message) {

        String body = buildFailJsonBody(message, status, System.currentTimeMillis());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(body, headers, status);
    }


    /**
     * Log the exceptions handled by the advice
     *
     * @param ex the exception to log
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void logHandledDebug(Exception ex) {
        log.debug("Exception Handled in the Advice: {}", ex == null ? "null" : ex.toString());
    }

    /**
     * Check if the user is authenticated
     *
     * @return true if is authenticated ( {@code principal instanceof UserWithId} )
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @see UserWithId
     * @since 1.0
     */
    private boolean isAuthenticated() {
        return (SecurityContextHolder.getContext().getAuthentication().getPrincipal()) instanceof UserWithId;
    }

    /**
     * Check if the user has the role {@link ERole#ROLE_ADMIN}
     *
     * @return true if the {@code principal} has the role {@link ERole#ROLE_ADMIN}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private boolean isAdmin() {
        if (!isAuthenticated()) return false;
        boolean isAdm = false;

        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (obj instanceof UserWithId) {
            UserWithId usr = (UserWithId) obj;
            if (usr.getAuthorities() == null || usr.getAuthorities().isEmpty()) return false;
            isAdm = usr
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a
                            .getAuthority()
                            .contains(ERole.ROLE_ADMIN.toString()));
        }
        return isAdm;
    }

    /**
     * Verify if the {@code msg.toLowerCase().trim()} contains all the
     * {@code strings[].trimEach().toLowerCase()}
     *
     * @param msg     that contains the {@code strings}
     * @param strings to verify if are in the {@code msg}
     * @return true if all the {@code strings} are in the {@code msg}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public boolean thisContains(String msg, String... strings) {
        if (msg == null || strings == null || strings.length == 0) return false;

        msg = msg.toLowerCase().trim();
        boolean contains = true;
        for (String s : strings) {
            s = s.toLowerCase().trim();
            contains = contains && msg.contains(s);
        }
        return contains;
    }

    /**
     * save the {@code UnauthenticatedException} in a file, also log an {@code ERROR}
     * if the exception is an exception threw in production.
     *
     * @param e the exception to log
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public void logUnhandledException(@NotNull Exception e) {
        boolean isTesting = this.thisContains(e.getMessage(), Cons.TESTING.UNHANDLED_ERROR_WITH_TESTING_PURPOSES);
        if (!isTesting)
            log.error("Unhandled exception: {}", (e.toString())); // print ERRORs in testing can be confusing
        saveUnhandledException(e);
    }

    /**
     * Save the unhandled exception in the file: {@link Cons.Logs#UNHANDLED_EXCEPTIONS_FILE}
     *
     * @param e the exception to save in the file
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void saveUnhandledException(Exception e) {
        Path path = Path.of(Cons.Logs.UNHANDLED_EXCEPTIONS_FILE);
        filesSyncUtils.appendToFile(
                path,
                new Date().toString() + "::" + e.toString() + "::" + Arrays.toString(e.getStackTrace()),
                SychFor.UNHANDLED_EXCEPTIONS
        );
    }
}