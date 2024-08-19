package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when the email already exists
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class EmailAlreadyExistsException extends ProperExceptionForTheUser {
    public EmailAlreadyExistsException() {
        super(HttpStatus.CONFLICT, Cons.User.Constrains.EMAIL_UNIQUE_MSG);
    }
}