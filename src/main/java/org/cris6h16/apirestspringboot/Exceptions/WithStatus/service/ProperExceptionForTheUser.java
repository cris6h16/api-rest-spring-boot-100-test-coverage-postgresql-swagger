package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * An exception that will be thrown with a status and a reason
 * that will be passed <strong>DIRECTLY</strong> to the user.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Getter
public class ProperExceptionForTheUser extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public ProperExceptionForTheUser(HttpStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}
