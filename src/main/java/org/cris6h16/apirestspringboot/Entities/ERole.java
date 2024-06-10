package org.cris6h16.apirestspringboot.Entities;

public enum ERole {
    /**
     * Used when we need to load a UserDetails from the database and the UserDetails
     * has no roles assigned
     */
    ROLE_INVITED,
    ROLE_USER,
    ROLE_ADMIN
}
