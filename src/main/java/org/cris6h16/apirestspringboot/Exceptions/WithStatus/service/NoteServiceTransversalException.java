package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service;

import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.springframework.http.HttpStatus;

/**
 * Exception exclusively designed to be thrown an exception
 * which will leave the {@link NoteServiceImpl} layer and
 * pass transversally through the next layer {@code Controller}.
 * this must be the unique exception that can be thrown to the next layer.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class NoteServiceTransversalException extends AbstractExceptionWithStatus {
    public NoteServiceTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);
    }
}
