package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when the id is invalid
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class InvalidIdException extends ProperExceptionForTheUser {
    public InvalidIdException() {
        super(HttpStatus.BAD_REQUEST, Cons.CommonInEntity.ID_INVALID);
    }
}
