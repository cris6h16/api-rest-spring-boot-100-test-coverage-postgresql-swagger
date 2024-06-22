package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UsernameAlreadyExistsException extends ProperExceptionForTheUser {
    public UsernameAlreadyExistsException() {
        super(HttpStatus.CONFLICT, Cons.User.Constrains.USERNAME_UNIQUE_MSG);
    }
}
