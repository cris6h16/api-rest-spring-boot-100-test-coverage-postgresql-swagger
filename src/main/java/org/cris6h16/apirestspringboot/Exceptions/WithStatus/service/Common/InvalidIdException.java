package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

public class InvalidIdException extends AbstractExceptionWithStatus {
    public InvalidIdException() {
        super(Cons.CommonInEntity.ID_INVALID, HttpStatus.BAD_REQUEST);
    }
}
