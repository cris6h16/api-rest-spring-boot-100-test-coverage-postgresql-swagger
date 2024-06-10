package org.cris6h16.apirestspringboot.Config.Security.UserDetailsService;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.Security.UserDetailsService.UserHasNotRolesException;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Optional;
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
     * Load the user from the database by the {@code username}.
     *
     * @param username The username of the user to load from the database.
     * @return The user loaded from the database.
     * @throws UsernameNotFoundException If the user is not found in the database.
     * @throws UserHasNotRolesException  If the user hasn't roles assigned.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, UserHasNotRolesException {
        // Find the user
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isEmpty()) throw new UsernameNotFoundException(Cons.User.Fails.NOT_FOUND);
        if (user.get().getRoles() == null) throw new UserHasNotRolesException(); // AbstractExceptionWithStatus

        // Collect the roles
        Collection<? extends GrantedAuthority> authorities = user.get().getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name())) // role.name is `@NotBlank`
                .collect(Collectors.toList());

        // Is enabled?
//        boolean enabled = user.get().getDeletedAt() == null;
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        return new UserWithId(
                user.get().getId(),
                user.get().getUsername(),
                user.get().getPassword(), // Password is encoded
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities
        );
    }
}
