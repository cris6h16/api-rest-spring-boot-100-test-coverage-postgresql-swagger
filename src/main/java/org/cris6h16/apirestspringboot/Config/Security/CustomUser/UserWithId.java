package org.cris6h16.apirestspringboot.Config.Security.CustomUser;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom class that extends {@link User} to add an {@code id} field to the user.
 * {@code id} should be used to identify the user in the database.
 * <br>
 * It'll also be used for verifications like:
 * <pre>{@code
 * ENDPOINT: GET /api/v1/users/{id}
 * REQUEST: GET /api/v1/users/1
 * THROW IF: #principal.id != 1
 * }</pre>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class UserWithId extends User {
    @Getter
    private Long id;

    public UserWithId(Long id, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
    }
}
