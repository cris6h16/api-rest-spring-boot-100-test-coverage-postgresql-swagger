package org.cris6h16.apirestspringboot.Exceptions.WithStatus.controller;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

/**
 * Exception exclusively designed to be thrown an exception
 * which will leave the {@link UserController} layer and
 * pass to be caught by the {@link ExceptionHandlerControllers} to be handled.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Slf4j
public class UserControllerTransversalException extends AbstractExceptionWithStatus {
    public UserControllerTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);

        log.debug(String.format(
                "A new instance of %s with: recommendedStatus=%s, message=%s. was created",
                this.getClass().getSimpleName(),
                recommendedStatus.toString(),
                message));
    }
}
