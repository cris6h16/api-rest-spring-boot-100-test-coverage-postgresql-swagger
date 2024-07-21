package org.cris6h16.apirestspringboot.DTOs.Creation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Users.NotNullAttributesToLowerConverter;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Users.NotNullAttributesTrimmer;


/**
 * DTO for creating a new user.
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
public class CreateUserDTO implements NotNullAttributesTrimmer, NotNullAttributesToLowerConverter {
    //    Verifications were centralized in the service layer(therefore theirs fail messages), and verified manually to avoid increase the testing complexity(I don't use the validator bean)
//    @NotBlank(message = USERNAME_LENGTH_FAIL_MSG)
//    @NotBlank(message = PASSWORD_LENGTH_FAIL_MSG)
//    @NotBlank(message = EMAIL_IS_INVALID_MDG)
    private String username;
    private String password;
    private String email;

    @Override
    public void trimNotNullAttributes() {
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
        if (email != null) email = email.trim();
    }

    @Override
    public void toLowerCaseNotNullAttributes() {
        if (username != null) username = username.toLowerCase();
        if (email != null) email = email.toLowerCase();
//        if (password != null) password = password.toLowerCase(); // password should not be lowercased
    }
}
