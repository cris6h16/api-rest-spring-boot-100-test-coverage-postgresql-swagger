package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.springframework.http.HttpStatus;

public class AnyUserDTOIsNullException extends ProperExceptionForTheUser {
    public AnyUserDTOIsNullException() {
        super(HttpStatus.BAD_REQUEST, Cons.User.DTO.ANY_RELATED_DTO_WITH_USER_NULL);
    }

}
