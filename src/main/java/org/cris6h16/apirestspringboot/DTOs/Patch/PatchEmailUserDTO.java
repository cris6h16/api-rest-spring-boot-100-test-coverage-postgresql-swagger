package org.cris6h16.apirestspringboot.DTOs.Patch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.EMAIL_INVALID_MSG;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.EMAIL_IS_BLANK_MSG;

/**
 * DTO to update the email of a user
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PatchEmailUserDTO {
    @Email(message = EMAIL_INVALID_MSG)// --> null is valid
    @NotBlank(message = EMAIL_IS_BLANK_MSG)
    private String email;
}
