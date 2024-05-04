package org.cris6h16.apirestspringboot.Config.Service.PreExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyExistsException extends ResponseStatusException {
    public AlreadyExistsException(String val) {
        super(HttpStatus.CONFLICT, val + " already exists");
    }
}
