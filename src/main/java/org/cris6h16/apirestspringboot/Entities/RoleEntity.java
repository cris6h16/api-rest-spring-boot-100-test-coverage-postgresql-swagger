package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_role_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ERole name;

}
