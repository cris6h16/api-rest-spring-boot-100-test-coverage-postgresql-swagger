package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AbstractServiceExceptionWithStatus{

    public UserNotFoundException() {
        super(Cons.User.Fails.NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
