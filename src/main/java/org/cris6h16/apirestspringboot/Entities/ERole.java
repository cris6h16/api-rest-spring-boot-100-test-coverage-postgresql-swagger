package org.cris6h16.apirestspringboot.Entities;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;

/**
 * Enum to represent the {@code roles} for the name of the {@link RoleEntity},
 * this is used also in the authorities for {@link UserWithId}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public enum ERole {
    ROLE_USER,
    ROLE_ADMIN
}
