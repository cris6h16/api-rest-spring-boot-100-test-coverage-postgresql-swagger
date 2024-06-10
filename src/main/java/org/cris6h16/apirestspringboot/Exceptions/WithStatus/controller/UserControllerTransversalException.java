package org.cris6h16.apirestspringboot.Exceptions.WithStatus.controller;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.springframework.http.HttpStatus;

// todo:
@Slf4j
public class UserControllerTransversalException extends AbstractExceptionWithStatus {
    public UserControllerTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);

        log.debug(String.format(
                "A new instance of %s with: recommendedStatus=%s, message=%s. was created",
                this.getClass().getSimpleName(),
                recommendedStatus.toString(),
                message));
    }
}
