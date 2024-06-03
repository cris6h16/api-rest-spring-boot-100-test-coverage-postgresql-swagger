package org.cris6h16.apirestspringboot.DTOs;

import lombok.*;
import org.cris6h16.apirestspringboot.Entities.ERole;

@AllArgsConstructor
@ToString
@Getter
@Builder
public class RoleDTO {
    private ERole name;
}
