package org.cris6h16.apirestspringboot.Exceptions.WithStatus.Security.UserDetailsService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

public class UserHasNotRolesException extends AbstractExceptionWithStatus  {
    public UserHasNotRolesException() {
        super(Cons.User.UserDetailsServiceImpl.USER_HAS_NOT_ROLES, HttpStatus.FORBIDDEN);
    }
}
