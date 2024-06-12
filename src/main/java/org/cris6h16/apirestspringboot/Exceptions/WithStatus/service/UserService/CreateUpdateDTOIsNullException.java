package org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.springframework.http.HttpStatus;
/**
 * Custom exception thrown when a {@link CreateUpdateUserDTO} is null
 * <p>
 * As extended of {@link AbstractExceptionWithStatus}, This will contain:<br>
 * - The status code is set: {@link HttpStatus#BAD_REQUEST}<br>
 * - The message is set: {@link Cons.User.DTO#NULL}
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Slf4j
public class CreateUpdateDTOIsNullException extends AbstractExceptionWithStatus {
    public CreateUpdateDTOIsNullException() {
        super(Cons.User.DTO.NULL, HttpStatus.BAD_REQUEST);

        log.debug(String.format(
                "A new instance of %s with: recommendedStatus=%s, message=%s. was created",
                this.getClass().getSimpleName(),
                super.getRecommendedStatus().toString(),
                super.getMessage()));
    }
}
