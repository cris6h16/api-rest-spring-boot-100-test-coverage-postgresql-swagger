package org.cris6h16.apirestspringboot.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;

// import Constants.Cons.User.*


/**
 * DTO for {@link UserEntity}
 * <p>
 * - Used for request a creation through the {@link UserService#create(CreateUpdateUserDTO)}<br>
 * - Also used to update through the {@link UserService#update(Long, CreateUpdateUserDTO)}
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
/* RestClientException: No HttpMessageConverter for CreateUserDTO
 * WHEN: rt.exchange(url, HttpMethod.POST, user, Void.class);
 * THEN: @JsonFormat  ->  indicate how your DTO is serialized and deserialized.
 */
@JsonFormat
public class CreateUpdateUserDTO {
    private String username;
    private String password;
    private String email;
}
