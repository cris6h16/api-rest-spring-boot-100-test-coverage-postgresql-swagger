package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.springframework.http.HttpStatus;

public class CreateNoteDTOIsNullException extends NoteServiceTransversalException {
    public CreateNoteDTOIsNullException() {
        super(Cons.Note.DTO.NULL, HttpStatus.BAD_REQUEST);
    }
}
