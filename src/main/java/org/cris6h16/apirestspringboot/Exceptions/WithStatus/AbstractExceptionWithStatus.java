package org.cris6h16.apirestspringboot.Exceptions.WithStatus;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public abstract class AbstractExceptionWithStatus extends RuntimeException {

    @Getter
    private HttpStatus recommendedStatus;

    public AbstractExceptionWithStatus(String message, HttpStatus recommendedStatus) {
        super(message);
        this.recommendedStatus = recommendedStatus;
    }

}

