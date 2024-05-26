package org.cris6h16.apirestspringboot.Service.PreExceptions;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException {
    public static class User extends ResponseStatusException {
        public User() {
            super(HttpStatus.NOT_FOUND, Cons.User.Fails.NOT_FOUND);
        }
    }
}
