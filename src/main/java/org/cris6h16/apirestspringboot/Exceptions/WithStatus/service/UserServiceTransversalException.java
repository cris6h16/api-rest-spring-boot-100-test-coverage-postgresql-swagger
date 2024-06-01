package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

//todo: docs that thiws will be used for pass to others layers
@Slf4j
public class UserServiceTransversalException extends AbstractExceptionWithStatus {
    public UserServiceTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);
    }
}
