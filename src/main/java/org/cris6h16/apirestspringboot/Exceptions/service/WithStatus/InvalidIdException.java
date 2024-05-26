package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;

public class InvalidIdException extends AbstractServiceExceptionWithStatus{
    public InvalidIdException() {
        super(Cons.Services.User.ID_INVALID, HttpStatus.BAD_REQUEST);
    }
}
