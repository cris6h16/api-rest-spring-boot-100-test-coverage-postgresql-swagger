package org.cris6h16.apirestspringboot.DTOs;

import lombok.*;
import org.cris6h16.apirestspringboot.Entities.ERole;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class RoleDTO {
    private ERole name;
}
