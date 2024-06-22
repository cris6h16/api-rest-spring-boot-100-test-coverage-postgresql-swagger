package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;
// the unique validation in the service due that the password
// is passed encrypted, then always is > 8
public class PasswordTooShortException extends ProperExceptionForTheUser {
    public PasswordTooShortException() {
        super(HttpStatus.BAD_REQUEST, Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG);
    }
}
