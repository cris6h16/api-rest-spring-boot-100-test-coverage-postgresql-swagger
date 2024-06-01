package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends UserServiceTransversalException {

    public UserNotFoundException() {
        super(Cons.User.Fails.NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
