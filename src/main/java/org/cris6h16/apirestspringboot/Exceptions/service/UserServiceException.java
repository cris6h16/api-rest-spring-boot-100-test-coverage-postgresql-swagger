package org.cris6h16.apirestspringboot.Exceptions.service;

import org.springframework.http.HttpStatus;

public class UserServiceException extends AbstractServiceException {
    private HttpStatus recommendedStatus;
    public UserServiceException(Exception e, HttpStatus recommendedStatus) {
        super(message);
    }
    public UserServiceException(String message, HttpStatus recommendedStatus) {
        super(message);
        this.recommendedStatus = recommendedStatus;
    }
}
