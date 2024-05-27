package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.NoteService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.springframework.http.HttpStatus;

public class NoteNotFoundException extends AbstractServiceExceptionWithStatus {
    public NoteNotFoundException() {
        super(Cons.Note.Fails.NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
