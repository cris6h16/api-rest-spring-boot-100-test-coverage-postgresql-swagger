package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Custom exception thrown when the requested note is not found.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class NoteNotFoundException extends ResponseStatusException {
    public NoteNotFoundException() {
        super(HttpStatus.NOT_FOUND, Cons.Note.Fails.NOT_FOUND);
    }
}
