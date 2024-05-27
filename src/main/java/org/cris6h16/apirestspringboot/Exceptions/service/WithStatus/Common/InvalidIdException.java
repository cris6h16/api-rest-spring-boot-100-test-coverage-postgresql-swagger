package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.Common;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.springframework.http.HttpStatus;

public class InvalidIdException extends AbstractServiceExceptionWithStatus {
    public InvalidIdException() {
        super(Cons.CommonInEntity.ID_INVALID, HttpStatus.BAD_REQUEST);
    }
}
