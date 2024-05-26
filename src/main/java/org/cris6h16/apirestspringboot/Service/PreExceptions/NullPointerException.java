package org.cris6h16.apirestspringboot.Service.PreExceptions;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NullPointerException {
    public static class CreateUpdateUserDTO extends ResponseStatusException {
        public CreateUpdateUserDTO() {
            super(HttpStatus.BAD_REQUEST, Cons.User.Fails.NULL);
        }
    }
}