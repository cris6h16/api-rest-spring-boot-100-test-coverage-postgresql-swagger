package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Custom exception thrown when a user is not found
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class UserNotFoundException extends ProperExceptionForTheUser {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, Cons.User.Fails.NOT_FOUND);
    }
}
