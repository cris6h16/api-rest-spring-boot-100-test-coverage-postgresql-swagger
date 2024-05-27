package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

//todo: docs that thiws will be used for pass to others layers
@Slf4j
public class UserServiceTraversalException extends AbstractServiceExceptionWithStatus {
    public UserServiceTraversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);
    }
}