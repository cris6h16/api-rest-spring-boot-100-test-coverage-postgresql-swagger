package org.cris6h16.apirestspringboot.DTOs.Patch;

import lombok.*;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Users.NotNullAttributesToLowerConverter;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Users.NotNullAttributesTrimmer;

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
public class PatchUsernameUserDTO implements NotNullAttributesTrimmer, NotNullAttributesToLowerConverter {
//    Verification was centralized in the service layer( and its message), and verified manually to avoid increase the testing complexity(I don't use the validator bean)
//    @NotBlank(message = EMAIL_IS_INVALID_MDG)
    private String username;

    @Override
    public void trimNotNullAttributes() {
        if (username != null) username = username.trim();
    }

    @Override
    public void toLowerCaseNotNullAttributes() {
        if (username != null) username = username.toLowerCase();
    }
}
