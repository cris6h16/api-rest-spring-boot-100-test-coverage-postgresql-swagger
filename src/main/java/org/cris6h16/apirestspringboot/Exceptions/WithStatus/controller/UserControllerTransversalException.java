package org.cris6h16.apirestspringboot.Exceptions.WithStatus.controller;

import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;

public class UserControllerTransversalException extends AbstractExceptionWithStatus {
    public UserControllerTransversalException(String message, HttpStatus recommendedStatus) {
        super(message, recommendedStatus);
    }
}
