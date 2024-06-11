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
    /**
     * Used when we need to load a UserDetails from the database and the UserDetails
     * has no roles assigned
     */
    ROLE_INVITED,
    ROLE_USER,
    ROLE_ADMIN
}
