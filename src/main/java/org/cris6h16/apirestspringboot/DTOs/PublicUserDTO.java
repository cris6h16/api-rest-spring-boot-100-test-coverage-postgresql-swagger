package org.cris6h16.apirestspringboot.DTOs;

import lombok.*;

import java.util.Date;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class PublicUserDTO {
    private String username;
    private String email;
    private Date createdAt;
    private Date updatedAt;
    private Set<RoleDTO> roles;
    private Set<PublicNoteDTO> notes;
}