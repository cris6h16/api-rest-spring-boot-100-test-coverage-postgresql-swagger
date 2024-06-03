package org.cris6h16.apirestspringboot.Config.Security.CustomUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UserWithId extends User {
    private Long id;

//    public UserWithId(String username, String password, Collection<? extends GrantedAuthority> authorities) {
//        super(username, password, authorities);
//    }

    public UserWithId(Long id, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
