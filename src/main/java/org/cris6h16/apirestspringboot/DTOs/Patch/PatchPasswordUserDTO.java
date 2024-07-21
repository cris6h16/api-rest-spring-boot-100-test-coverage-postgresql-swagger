package org.cris6h16.apirestspringboot.DTOs.Patch;

import lombok.*;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Users.NotNullAttributesTrimmer;

/**
 * DTO to update the password of a user
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PatchPasswordUserDTO implements NotNullAttributesTrimmer {
//    Verification was centralized in the service layer( and its message), and verified manually to avoid increase the testing complexity(I don't use the validator bean)
//    @NotBlank(message = Cons.User.Validations.PASSWORD_LENGTH_FAIL_MSG)
    private String password;

    @Override
    public void trimNotNullAttributes() {
        if (password != null) password = password.trim();
    }

}
