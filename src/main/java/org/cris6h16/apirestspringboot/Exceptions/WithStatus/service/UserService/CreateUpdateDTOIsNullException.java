package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.springframework.http.HttpStatus;

public class CreateUpdateDTOIsNullException extends AbstractExceptionWithStatus {
    public CreateUpdateDTOIsNullException() {
        super(Cons.User.DTO.NULL, HttpStatus.BAD_REQUEST);
    }
}
