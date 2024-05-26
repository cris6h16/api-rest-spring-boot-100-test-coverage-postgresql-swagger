package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.springframework.http.HttpStatus;

public class CreateUpdateDTOIsNullException extends AbstractServiceExceptionWithStatus {
    public CreateUpdateDTOIsNullException() {
        super(Cons.User.DTO.NULL, HttpStatus.BAD_REQUEST);
    }
}
