package org.cris6h16.apirestspringboot.Exceptions.service;

public abstract class AbstractServiceException extends RuntimeException {
    public AbstractServiceException(String message) {
        super(message);
    }
}
