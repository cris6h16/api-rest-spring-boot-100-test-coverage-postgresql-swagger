package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmailAlreadyExistException extends ResponseStatusException {
    public EmailAlreadyExistException() {
        super(HttpStatus.CONFLICT, Cons.User.Constrains.EMAIL_UNIQUE_MSG);
    }
}