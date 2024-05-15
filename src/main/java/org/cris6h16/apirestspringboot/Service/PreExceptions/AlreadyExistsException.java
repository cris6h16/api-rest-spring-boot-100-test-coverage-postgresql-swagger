package org.cris6h16.apirestspringboot.Service.PreExceptions;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyExistsException {

    public static class Username extends ResponseStatusException {
        public Username() {
            super(HttpStatus.CONFLICT, Cons.User.Constrains.USERNAME_UNIQUE_MSG);
        }



    }

    public static class Email extends ResponseStatusException {
        public Email() {
            super(HttpStatus.CONFLICT, Cons.User.Constrains.EMAIL_UNIQUE_MSG);
        }
    }
}
