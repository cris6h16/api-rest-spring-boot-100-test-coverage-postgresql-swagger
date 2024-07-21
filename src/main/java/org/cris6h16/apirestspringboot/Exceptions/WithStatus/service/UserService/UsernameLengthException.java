package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

public class UsernameLengthException extends ProperExceptionForTheUser {
    public UsernameLengthException() {
        super(HttpStatus.BAD_REQUEST, Cons.User.Validations.USERNAME_LENGTH_FAIL_MSG);
    }
}
