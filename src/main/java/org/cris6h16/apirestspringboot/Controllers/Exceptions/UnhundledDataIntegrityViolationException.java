package org.cris6h16.apirestspringboot.Controllers.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class UnhundledDataIntegrityViolationException extends ResponseStatusException {
    public UnhundledDataIntegrityViolationException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "This Data integrity violation wasn't handled. Please contact the administrator.");
    }
}
