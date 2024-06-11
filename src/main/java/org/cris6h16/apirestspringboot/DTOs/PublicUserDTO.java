package org.cris6h16.apirestspringboot.DTOs;

import lombok.*;
import org.cris6h16.apirestspringboot.Entities.UserEntity;

import java.util.Date;
import java.util.Set;

/**
 * DTO for {@link UserEntity} with relevant information for public access.<br>
 * comparing with the entity, this DTO just hides the password field.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@AllArgsConstructor
@ToString
@Getter
@Builder
public class PublicUserDTO {
    private Long id;
    private String username;
    private String email;
    private Date createdAt;
    private Date updatedAt;
    private Set<PublicRoleDTO> roles;
    private Set<PublicNoteDTO> notes;
}
