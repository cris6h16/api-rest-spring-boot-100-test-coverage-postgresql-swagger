package org.cris6h16.apirestspringboot.DTOs.Creation;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.*;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.EMAIL_IS_BLANK_MSG;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG;

// import Constants.Cons.User.*


/**
 * DTO for {@link UserEntity}
 * <p>
 * - Used for request a creation through the {@link UserService#create(CreateUserDTO)}<br>
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
/* RestClientException: No HttpMessageConverter for CreateUserDTO
 * WHEN: rt.exchange(url, HttpMethod.POST, user, Void.class);
 * THEN: @JsonFormat  ->  indicate how your DTO is serialized and deserialized.
 */
@JsonFormat
public class CreateUserDTO {
    @NotBlank(message = USERNAME_IS_BLANK_MSG)
    private String username;

    @NotBlank(message = PASS_IS_TOO_SHORT_MSG)
    private String password; // pass is passed encrypted, then always is > 8

    @NotBlank(message = EMAIL_IS_BLANK_MSG)
    private String email;
}
