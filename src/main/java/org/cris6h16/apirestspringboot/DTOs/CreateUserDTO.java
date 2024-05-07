package org.cris6h16.apirestspringboot.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.Set;

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
    @NotBlank(message = "Username mustn't be blank") // for sending null/empty
    private String username;



    @NotBlank(message = "Password is required")
//    @Length(min = 8, message = "Password must be at least 8 characters") --> we handled it directly in @Service
    // min doesn't work very well, remember that is saved encrypted
    private String password;

    @Email(message = "Email is invalid") // null is valid
    @NotBlank(message = "Email is required")
    private String email;

}
