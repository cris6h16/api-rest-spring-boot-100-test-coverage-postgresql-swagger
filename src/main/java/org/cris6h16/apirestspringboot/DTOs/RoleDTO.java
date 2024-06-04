package org.cris6h16.apirestspringboot.DTOs;

import lombok.*;
import org.cris6h16.apirestspringboot.Entities.ERole;

@AllArgsConstructor
@ToString
@Getter
@Builder
@EqualsAndHashCode
public class RoleDTO {
    private ERole name;
}
