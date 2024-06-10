package org.cris6h16.apirestspringboot.Exceptions.WithStatus.Security.UserDetailsService;

import org.cris6h16.apirestspringboot.Config.Security.UserDetailsService.UserDetailsServiceImpl;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when {@code user.roles == null}
 * exception exclusively used in {@link UserDetailsServiceImpl#loadUserByUsername(String)}
 * <p>
 * As extended of {@link AbstractExceptionWithStatus}, This will contain:<br>
 * - The status code is set: {@link HttpStatus#FORBIDDEN}<br>
 * - The message is set: {@link Cons.CommonInEntity#ID_INVALID}
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class UserHasNullRolesException extends AbstractExceptionWithStatus {
    public UserHasNullRolesException() {
        super(Cons.User.UserDetailsServiceImpl.USER_HAS_NOT_ROLES, HttpStatus.FORBIDDEN);
    }
}
