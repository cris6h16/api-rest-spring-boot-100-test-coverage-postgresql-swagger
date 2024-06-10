package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when an invalid entity ID is encountered.
 * <p>
 * As extended of {@link AbstractExceptionWithStatus}, This will contain:<br>
 * - The status code is set: {@link HttpStatus#BAD_REQUEST}<br>
 * - The message is set: {@link Cons.CommonInEntity#ID_INVALID}
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class InvalidIdException extends AbstractExceptionWithStatus {
    public InvalidIdException() {
        super(Cons.CommonInEntity.ID_INVALID, HttpStatus.BAD_REQUEST);
    }
}
