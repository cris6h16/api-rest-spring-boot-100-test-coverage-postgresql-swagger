package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ProperExceptionForTheUser extends RuntimeException {
    private HttpStatus status;
    private String reason;

    public ProperExceptionForTheUser(HttpStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}
