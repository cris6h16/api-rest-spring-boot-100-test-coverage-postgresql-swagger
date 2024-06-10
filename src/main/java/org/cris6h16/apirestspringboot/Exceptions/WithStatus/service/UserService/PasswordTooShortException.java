package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.springframework.http.HttpStatus;

public class PasswordTooShortException extends AbstractExceptionWithStatus {
    public PasswordTooShortException() {
        super(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG, HttpStatus.BAD_REQUEST);
    }
}
