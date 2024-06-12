package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when a user is not found
 * <p>
 * As extended of {@link AbstractExceptionWithStatus}, This will contain:<br>
 * - The status code is set: {@link HttpStatus#NOT_FOUND}<br>
 * - The message is set: {@link Cons.User.Fails#NOT_FOUND}
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Slf4j
public class UserNotFoundException extends AbstractExceptionWithStatus {
    public UserNotFoundException() {
        super(Cons.User.Fails.NOT_FOUND, HttpStatus.NOT_FOUND);

        log.debug(String.format(
                "A new instance of %s with: recommendedStatus=%s, message=%s. was created",
                this.getClass().getSimpleName(),
                super.getRecommendedStatus().toString(),
                super.getMessage()));
    }
}
