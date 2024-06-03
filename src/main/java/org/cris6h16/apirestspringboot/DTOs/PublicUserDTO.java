package org.cris6h16.apirestspringboot.DTOs;

import lombok.*;

import java.util.Date;
import java.util.Set;

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
    private Set<RoleDTO> roles;
    private Set<PublicNoteDTO> notes;
}
