package org.cris6h16.apirestspringboot.Controllers.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AttributesRequiredAreNullException extends ResponseStatusException {
    public AttributesRequiredAreNullException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
