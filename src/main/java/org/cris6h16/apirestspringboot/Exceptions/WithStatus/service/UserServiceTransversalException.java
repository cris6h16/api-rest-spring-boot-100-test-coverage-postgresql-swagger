package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.springframework.http.HttpStatus;

/**
 * Exception exclusively designed to be thrown an exception
 * which will leave the {@link UserServiceImpl} layer and
 * pass transversally through the next layer ({@link UserController}),
 * this must be the unique exception that can be thrown to the next layer.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Slf4j
public class UserServiceTransversalException extends AbstractExceptionWithStatus {
    public UserServiceTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);

        log.debug(String.format(
                "A new instance of %s with: recommendedStatus=%s, message=%s. was created",
                this.getClass().getSimpleName(),
                recommendedStatus.toString(),
                message));
    }
}
