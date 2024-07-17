package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when the username already exists
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class UsernameAlreadyExistsException extends ProperExceptionForTheUser {
    public UsernameAlreadyExistsException() {
        super(HttpStatus.CONFLICT, Cons.User.Constrains.USERNAME_UNIQUE_MSG);
    }
}
