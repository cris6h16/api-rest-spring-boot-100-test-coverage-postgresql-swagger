package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.Principal;

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

    /**
     * Handling of {@link AbstractExceptionWithStatus}
     *
     * @param ex the exception
     * @return A {@link ResponseEntity} with status code && message
     * gotten from the mentioned exception
     */
    @ExceptionHandler(value = {AbstractExceptionWithStatus.class})
    public ResponseEntity<String> handleServiceExceptionWithStatus(AbstractExceptionWithStatus ex) {
        return buildAFailResponse(ex.getRecommendedStatus(), ex.getMessage());
    }


    /**
     * Handling of {@link NoResourceFoundException}
     *
     * @param ex exception
     * @return a {@link ResponseEntity} containing the status {@link HttpStatus#NOT_FOUND}
     * and the message {@link Cons.Response.ForClient#NO_RESOURCE_FOUND}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @note Added thanks to {@link ServiceUtils#logUnhandledException(Exception)}
     * @since 1.0
     */
    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
        logDebug(ex);
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
     * @return A {@link ResponseEntity} with status {@link HttpStatus#FORBIDDEN}
     * and message {@link  Cons.Auth.Fails#ACCESS_DENIED}
     * @note Added thanks to {@link ServiceUtils#logUnhandledException(Exception)}
     * @implNote the response is diff if is authenticated or not, this is verified seeing
     * if the {@link Principal} is an instance of {@link UserWithId}, that means that my
     * custom {@link UserDetails} was loaded ( authenticated correctly )
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = AccessDeniedException.class) // added thanks to the logs (ERROR)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        logDebug(ex);

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
     *
     * </p>
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with status {@link HttpStatus#BAD_REQUEST}
     * and messages {@link Cons.Response.ForClient#GENERIC_ERROR}
     * @note Added thanks to {@link ServiceUtils#logUnhandledException(Exception)}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        logDebug(ex);
        return buildAFailResponse(HttpStatus.BAD_REQUEST, Cons.Response.ForClient.GENERIC_ERROR);
    }

    /**
     * Handling of {@link HttpMediaTypeNotSupportedException}
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with status with the proper status code
     * @note Added thanks to {@link ServiceUtils#logUnhandledException(Exception)}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        logDebug(ex);
        return buildAFailResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE);
    }


    /**
     * Handling of generic exceptions
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with status {@link HttpStatus#INTERNAL_SERVER_ERROR}
     * and message {@link Cons.Response.ForClient#GENERIC_ERROR}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception ex) {
        logDebug(ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unhandled exception caught in Advice: {}", ex.toString());
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
     * Log the exception caught in the advice
     *
     * @param ex the exception to log
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void logDebug(Exception ex) {
        log.debug("Exception caught in the Advice: {}", ex == null ? "null" : ex.toString());
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

}