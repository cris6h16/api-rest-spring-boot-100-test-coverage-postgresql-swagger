package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.springframework.http.HttpStatus;

public class PasswordTooShortException extends AbstractServiceExceptionWithStatus {
    public PasswordTooShortException() {
        super(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG, HttpStatus.BAD_REQUEST);
    }
}
