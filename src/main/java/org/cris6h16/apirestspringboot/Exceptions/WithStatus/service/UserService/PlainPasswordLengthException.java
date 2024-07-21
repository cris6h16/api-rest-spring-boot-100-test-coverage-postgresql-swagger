package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

public class PlainPasswordLengthException extends ProperExceptionForTheUser {
    public PlainPasswordLengthException() {
        super(HttpStatus.BAD_REQUEST, Cons.User.Validations.PASSWORD_LENGTH_FAIL_MSG);
    }
}
