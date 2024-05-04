package org.cris6h16.apirestspringboot.Config.Service.PreExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PasswordIsTooShortException extends ResponseStatusException {
    public PasswordIsTooShortException() {
        super(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
    }
}
