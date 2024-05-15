package org.cris6h16.apirestspringboot.Service.PreExceptions;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PasswordIsTooShortException extends ResponseStatusException {
    public PasswordIsTooShortException() {
        super(HttpStatus.BAD_REQUEST, Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG);
    }
}
