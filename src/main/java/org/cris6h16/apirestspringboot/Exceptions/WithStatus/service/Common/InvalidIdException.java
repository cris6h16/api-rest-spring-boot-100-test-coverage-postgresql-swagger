package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

public class InvalidIdException extends ProperExceptionForTheUser {
    public InvalidIdException() {
        super(HttpStatus.BAD_REQUEST, Cons.CommonInEntity.ID_INVALID);
    }
}
