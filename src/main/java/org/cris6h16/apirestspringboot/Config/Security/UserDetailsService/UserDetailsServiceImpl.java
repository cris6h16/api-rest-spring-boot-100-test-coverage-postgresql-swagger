package org.cris6h16.apirestspringboot.Config.Security.UserDetailsService;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Custom implementation of {@link UserDetailsService} to load the user from the database.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;


    public UserDetailsServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Load the user from the database by the {@code username}.<br>
     * if {@code user.roles == null || user.roles.isEmpty()} then assign a role
     * default role {@link ERole#ROLE_INVITED}
     *
     * @param username of the user to load from the database.
     * @return The user loaded from the database.
     * @throws UsernameNotFoundException If the user is not found in the database.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(Cons.User.Fails.NOT_FOUND));

        // If the user hasn't roles assigned, assign a role that is considered as "invited"
        if (user.getRoles() == null || user.getRoles().isEmpty() ) {
            user.setRoles(new HashSet<>(Collections.singleton(
                    RoleEntity.builder().name(ERole.ROLE_INVITED).build()
            )));
        }

        // Collect the roles
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name())) // role.name is `@NotBlank`
                .collect(Collectors.toList());

        // Is enabled?
//        boolean enabled = user.getById().getDeletedAt() == null;
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        return new UserWithId(
                user.getId(),
                user.getUsername(),
                user.getPassword(), // Password is encoded
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities
        );
    }
}
