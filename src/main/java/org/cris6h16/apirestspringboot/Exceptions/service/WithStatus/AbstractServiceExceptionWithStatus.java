package org.cris6h16.apirestspringboot.Exceptions.service.WithStatus;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public abstract class AbstractServiceExceptionWithStatus extends RuntimeException {

    @Getter
    private HttpStatus recommendedStatus;

    public AbstractServiceExceptionWithStatus(String message, HttpStatus recommendedStatus) {
        super(message);
        this.recommendedStatus = recommendedStatus;
    }

}

