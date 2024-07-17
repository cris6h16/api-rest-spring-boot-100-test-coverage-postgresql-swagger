package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when the title is blank
 *
 * @since 1.0
 * */
public class AnyNoteDTOIsNullException extends ProperExceptionForTheUser {
    public AnyNoteDTOIsNullException() {
        super(HttpStatus.BAD_REQUEST, Cons.Note.DTO.NULL);
    }
}
