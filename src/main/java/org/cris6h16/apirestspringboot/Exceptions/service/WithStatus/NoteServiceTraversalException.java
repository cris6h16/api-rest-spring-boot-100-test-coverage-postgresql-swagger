package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus;

import org.springframework.http.HttpStatus;

public class NoteServiceTraversalException extends AbstractServiceExceptionWithStatus{
    public NoteServiceTraversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);
    }
}
