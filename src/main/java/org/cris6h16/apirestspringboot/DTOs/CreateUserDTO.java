package org.cris6h16.apirestspringboot.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
/*   RestClientException: No HttpMessageConverter for CreateUserDTO
 * WHEN: rt.exchange(url, HttpMethod.POST, user, Void.class);
 */
@JsonFormat // how your DTO is serialized and deserialized.
public class CreateUserDTO {
    @NotBlank(message = "Username mustn't be blank") // for sending null/empty
    private String username;



    @NotBlank(message = "Password is required")
    @Length(min = 8, message = "Password must be at least 8 characters")
    // min doesn't work very well, remember that is saved encrypted
    private String password;

    @Email(message = "Email is invalid") // null is valid
    @NotBlank(message = "Email is required")
    private String email;
}
