package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.NoteService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.springframework.http.HttpStatus;

public class CreateNoteDTOIsNullException extends AbstractServiceExceptionWithStatus {
    public CreateNoteDTOIsNullException() {
        super(Cons.Note.DTO.NULL, HttpStatus.BAD_REQUEST);
    }
}
