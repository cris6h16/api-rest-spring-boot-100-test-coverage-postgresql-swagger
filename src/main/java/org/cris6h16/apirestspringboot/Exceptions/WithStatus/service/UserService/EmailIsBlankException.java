package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

public class EmailIsBlankException extends ProperExceptionForTheUser {
    public EmailIsBlankException() {
        super(HttpStatus.BAD_REQUEST, Cons.User.Validations.EMAIL_IS_BLANK_MSG);
    }
}

