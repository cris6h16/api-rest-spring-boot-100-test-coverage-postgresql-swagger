package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.springframework.http.HttpStatus;

public class NoteNotFoundException extends NoteServiceTransversalException {
    public NoteNotFoundException() {
        super(Cons.Note.Fails.NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
