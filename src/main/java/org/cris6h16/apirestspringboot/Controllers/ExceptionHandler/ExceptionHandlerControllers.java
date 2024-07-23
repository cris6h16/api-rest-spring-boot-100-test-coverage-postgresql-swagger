package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.cris6h16.apirestspringboot.Utils.FilesUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handling of exception in the controllers
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerControllers {

    private final Object lock = new Object();
    private final FilesUtils filesSyncUtils;
    protected static volatile long lastSavedToFile; // concurrent changed
    protected static final long MILLIS_EACH_SAVE = 10 * 60 * 1000; // 10 minutes
    protected static List<String> hiddenExceptionsLines;

    public ExceptionHandlerControllers(FilesUtils filesSyncUtils) {
        this.filesSyncUtils = filesSyncUtils;
        lastSavedToFile = 0;
        hiddenExceptionsLines = new Vector<>(); // for thread safety ( for avoid internally crashes [adding && removing] )
    }

    /**
     * Handles the exceptions that was designed to be passed directly to the user
     *
     * @param e the exception
     * @return a {@link ResponseEntity} with the status and message of the exception
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(ProperExceptionForTheUser.class)
    public ResponseEntity<String> handleProperExceptionForTheUser(ProperExceptionForTheUser e) {
        logHandledDebug(e);
        return buildAFailResponse(e.getStatus(), e.getReason());
    }


    /**
     * Handling of generic exceptions
     *
     * @param e the exception
     * @return a {@link ResponseEntity} with status {@link HttpStatus#INTERNAL_SERVER_ERROR}
     * and message {@link Cons.Response.ForClient#GENERIC_ERROR} if is not an admin.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception e) {
        logHandledDebug(e);
        if (isAdmin()) return buildFailResponseForAdmin(e);
        else {
            saveHiddenExceptionForTheUserEveryDefinedMins(e);
            return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.FORBIDDEN); // simulate a Response<Void>, I cannot putByIdAndUserId that as return type because if is admin, the response will contain a String, also returning something in the body for !admins this can be mapped for know the existent endpoints( i.g. if the bad user make a request to an /admin endpoint the response will be a 403 Forbidden with empty body, but if the user make a request to an endpoint that doesn't exist (NoResourceFoundException) it will be a 403 Forbidden with a body that I decide pass here. So, the user can know the existent endpoints)
        }
    }

    /**
     * Build a {@link ResponseEntity<String>} to a failed request
     * for the admin
     *
     * @param e the exception to log
     * @return a containing with the status {@link HttpStatus#INTERNAL_SERVER_ERROR}
     * and in the message {@code @exception.toString}, with {@link MediaType#APPLICATION_JSON} as content type
     * @see #buildFailJsonBody(String, HttpStatus)
     * @since 1.0
     */
    private ResponseEntity<String> buildFailResponseForAdmin(Exception e) {
        String body = buildFailJsonBody(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Build a json body for a response of a failed request
     *
     * @param message value for the {@code message} in the json body
     * @param status  value for the {@code status} in the json body
     * @return a json body with the given values
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private String buildFailJsonBody(String message,
                                     HttpStatus status) {
        String statusStr = (status == null) ? "" : status.toString();
        message = (message == null) ? "" : message;

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                statusStr,
                DateTimeFormatter.ISO_INSTANT
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now()) // yyyy-MM-dd'T'HH:mm:ss.SSS'Z' --> ISO-8601 (UTC)
        );

        return String.format(
                removeChars(
                        """
                                {
                                    "message": "%s",
                                    "status": "%s",
                                    "instant": "%s"
                                }
                                """,
                        '\n', ' '),
                errorResponse.message(),
                errorResponse.status(),
                errorResponse.instant()
        );
    }

    private String removeChars(String str, char... chars) {
        if (str == null || chars == null || chars.length == 0) return str;
        StringBuilder sb = new StringBuilder(str);
        for (char c : chars) {
            int idx = sb.indexOf(String.valueOf(c));
            while (idx != -1) {
                sb.deleteCharAt(idx);
                idx = sb.indexOf(String.valueOf(c));
            }
        }
        return sb.toString();
    }

    /**
     * Build a {@link ResponseEntity<String>} for a failed request
     *
     * @param status  the proper status related to the exception
     * @param message the message to be shown in the response
     * @return containing the given status and message ready for show to
     * the client, with {@link MediaType#APPLICATION_JSON} as content type
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private ResponseEntity<String> buildAFailResponse(HttpStatus status, String message) {

        String body = buildFailJsonBody(message, status);

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
    private boolean _isAuthenticated() {
        try { // in testing the the above is null pointer exception
            return (SecurityContextHolder.getContext().getAuthentication().getPrincipal()) instanceof UserWithId;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the user has the role {@link ERole#ROLE_ADMIN}
     *
     * @return true if the {@code principal} has the role {@link ERole#ROLE_ADMIN}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private boolean isAdmin() {
        if (!_isAuthenticated()) return false;
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
     * Save the unhandled exception in the file: {@link Cons.Logs#HIDEN_EXCEPTION_OF_USERS}
     *
     * @param e the exception to save in the file
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void saveHiddenExceptionForTheUserEveryDefinedMins(Exception e) {
        synchronized (lock) {
            StringBuilder line = new StringBuilder()
                    .append(new Date().toString())
                    .append("::")
                    .append(e.toString())
                    .append("::")
                    .append(
                            (e.getStackTrace() == null || e.getStackTrace().length == 0) ? "" :
                                    Arrays.toString(e.getStackTrace()).substring(
                                            0, Math.min(e.getStackTrace().length, 100)
                                    )
                    );
            hiddenExceptionsLines.add(line.toString());
        }

        if (System.currentTimeMillis() - lastSavedToFile < MILLIS_EACH_SAVE) return;

        synchronized (lock) {
            if (System.currentTimeMillis() - lastSavedToFile < MILLIS_EACH_SAVE) return;

            StringBuilder content = new StringBuilder();
            for (String str : hiddenExceptionsLines) {
                content.append(str).append("\n");
            }
            hiddenExceptionsLines.clear();
            lastSavedToFile = System.currentTimeMillis();

            filesSyncUtils.appendToFile(
                    Path.of(Cons.Logs.HIDEN_EXCEPTION_OF_USERS),
                    content.toString()
            );
        }
    }

}
