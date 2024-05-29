package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus;

import org.springframework.http.HttpStatus;

public class NoteServiceTransversalException extends AbstractServiceExceptionWithStatus{
    public NoteServiceTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);
    }
}
