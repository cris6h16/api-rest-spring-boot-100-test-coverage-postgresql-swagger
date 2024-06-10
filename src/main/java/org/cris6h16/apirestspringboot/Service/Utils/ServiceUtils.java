package org.cris6h16.apirestspringboot.Service.Utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;

/**
 * Utility class for process the exceptions in the service layer methods,
 * this class is used to pass from an exception ( sometimes with critical information )
 * to an extended exception of {@link AbstractExceptionWithStatus} with
 * a proper message and status ready to pass through the layers
 * even ready to be sent directly to the client.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @implNote The existent methods are concurrently, There isn't synchronization
 * in the methods, I'm using local variables for sure the immutability
 * then all are thread-safe.
 * @since 1.0
 */
@Slf4j
@Component
public class ServiceUtils {


    /**
     * Create a new exception (Extended of {@link AbstractExceptionWithStatus})
     * with a proper message and status ready to be sent directly to the client.
     *
     * @param e             the exception to handle
     * @param isUserService if true return a {@link UserServiceTransversalException} else a {@link NoteServiceTransversalException}
     * @return a new TransversalException with the proper message and status ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public AbstractExceptionWithStatus createATraversalExceptionHandled(@NotNull Exception e, boolean isUserService) {
        String forClient = ""; // PD: verification based on: .isBlank(), dont add generic message here
        HttpStatus recommendedStatus = null; // also here, but with null

        // ------------ commons in both entities --------------------\\
        // data integrity violations { not blank, invalid email, max length, etc }
        if (e instanceof ConstraintViolationException && forClient.isBlank()) {
            recommendedStatus = HttpStatus.BAD_REQUEST;
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e).getConstraintViolations();

            if (!violations.isEmpty()) forClient = violations.iterator().next().getMessage();
        }

        // Note Service: { user not found, invalid id }
        // UserService:  { user not found, invalid id, password too short }
        if (e instanceof AbstractExceptionWithStatus && forClient.isBlank()) {
            recommendedStatus = ((AbstractExceptionWithStatus) e).getRecommendedStatus();
            forClient = e.getMessage();
        }

        if (e instanceof NullPointerException && forClient.isBlank()) {
            boolean pageablePassedNull = this.thisContains(e.getMessage(), "because \"pageable\" is null");
            if (pageablePassedNull) {
                recommendedStatus = HttpStatus.BAD_REQUEST;
                // forClient => empty isn't necessary more info
            }
        }

        if (e instanceof PropertyReferenceException && forClient.isBlank()) {
            recommendedStatus = HttpStatus.BAD_REQUEST;
            boolean propertyNonexistent = this.thisContains(e.getMessage(), "for type");
            if (propertyNonexistent) {
                forClient = e.getMessage().split("for type")[0].trim(); // No property 'ttt' found for type 'UserEntity'
            }
        }


        // ------------ response regard to the entity ------------- \\
        if (isUserService) {
            // unique violations { primary key, unique constraints }
            if (e instanceof DataIntegrityViolationException && forClient.isBlank()) {
                recommendedStatus = HttpStatus.CONFLICT;
                boolean inUsername = thisContains(e.getMessage(), USERNAME_UNIQUE_NAME);
                boolean inEmail = thisContains(e.getMessage(), EMAIL_UNIQUE_NAME);
                boolean isHandledUniqueViolation = inUsername || inEmail;

                if (isHandledUniqueViolation) forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;
            }

        } else { // ------- is Note Service ------- \\
            // now for note service I don't have custom exceptions, here
            // can be added exceptions like DataIntegrityViolationException if we have unique constraints
        }

        // -------------- default handling ----------------- \\
        {
            boolean recommendedStatusIsNull = recommendedStatus == null;
            boolean forClientIsBlank = forClient.isBlank();
            if (recommendedStatusIsNull) recommendedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            if (forClientIsBlank)
                forClient = Cons.Response.ForClient.GENERIC_ERROR; /* something I just want to tell to the client about the error(BAD_REQUEST, CONFLICT, etc) but not give more info, it can be considered as handled*/
            if (recommendedStatusIsNull)
                logUnhandledException(e); /* just verify `recommendedStatus`, because in all handled exceptions I set it, the `forClient` can be considered as nullable */
            else logDebug(e);
        }


        return isUserService ?
                new UserServiceTransversalException(forClient, recommendedStatus) :
                new NoteServiceTransversalException(forClient, recommendedStatus);
    }


    /**
     * Verify if the {@code msg.toLowerCase().trim()} contains all the
     * {@code strings.toLowerCase().trim()}
     *
     * @param msg     that contains the {@code strings}
     * @param strings to verify if are in the {@code msg}
     * @return true if all the {@code strings} are in the {@code msg}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public boolean thisContains(String msg, String... strings) {
        msg = msg.toLowerCase().trim();
        boolean contains = true;
        for (String s : strings) {
            s = s.toLowerCase().trim();
            contains = contains && msg.contains(s);
        }
        return contains;
    }

    public void logUnhandledException(Exception e) {
        log.error("Unhandled exception: {}", e.toString());
        if (!ignoreStackTrace(e))
            e.printStackTrace(); // todo: replace the printing of stackTrace with a log file or like that
    }

    private void logDebug(Exception e) {
        log.debug("Handled exception: {}", e.toString());
    }

    private boolean ignoreStackTrace(Exception e) {
        boolean randomExceptionForTesting = e != null &&
                e.getMessage() != null &&
                e.getMessage().toLowerCase().contains(Cons.TESTING.NOT_PRINT_STACK_TRACE_PATTERN.toLowerCase());
//        boolean exceptionHandledButDueToUseH2ForTestingTheException
        return randomExceptionForTesting;
    }

    /**
     * @param id to validate
     * @throws InvalidIdException impl if id is null or less than 1
     */
    public void validateId(Long id) {
        if (id == null || id <= 0)
            throw new InvalidIdException();
    }
}
