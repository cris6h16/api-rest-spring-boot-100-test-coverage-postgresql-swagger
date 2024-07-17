package org.cris6h16.apirestspringboot.DTOs.Patch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.*;

/**
 * DTO to update the username of a user
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PatchUsernameUserDTO {
    @Size(max = MAX_USERNAME_LENGTH, message = USERNAME_MAX_LENGTH_MSG) //--> null is valid
    @NotBlank(message = USERNAME_IS_BLANK_MSG)
    private String username;
}
