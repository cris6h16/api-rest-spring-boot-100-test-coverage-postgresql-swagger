package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service;

import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

// todo: make a diagram of the hierarchy of exceptions explained the use for the logging
public class NoteServiceTransversalException extends AbstractExceptionWithStatus {
    public NoteServiceTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);
    }
}
