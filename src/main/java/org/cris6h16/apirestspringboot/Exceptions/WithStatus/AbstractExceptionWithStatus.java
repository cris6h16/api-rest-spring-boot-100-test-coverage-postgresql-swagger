package org.cris6h16.apirestspringboot.Exceptions.WithStatus;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Base class for all application-specific exceptions.<br>
 * All business logic exceptions in the application should extend this class.
 *
 * <p>This class provides a mechanism to associate an HTTP status (and by default a message)
 * with the exception, which can be used to inform clients of the nature of the error.</p>
 *
 * <p>
 * Defined for contain a Status && a message ready to be sent directly to the client
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Slf4j
public abstract class AbstractExceptionWithStatus extends RuntimeException {

    @Getter
    private HttpStatus recommendedStatus;

    public AbstractExceptionWithStatus(String message, HttpStatus recommendedStatus) {
        super(message);
        this.recommendedStatus = recommendedStatus;

        log.debug(String.format(
                "A new extended instance of %s with: recommendedStatus=%s, message=%s. was created",
                this.getClass().getSimpleName(),
                recommendedStatus.toString(),
                message));
    }
}


