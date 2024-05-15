package org.cris6h16.apirestspringboot.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

// import Constants.Cons.User.*
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.*;


@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
/*   RestClientException: No HttpMessageConverter for CreateUserDTO
 * WHEN: rt.exchange(url, HttpMethod.POST, user, Void.class);
 */
@JsonFormat // how your DTO is serialized and deserialized.
@Builder
public class CreateUserDTO {
    private String username;
    private String password;
    private String email;
}
