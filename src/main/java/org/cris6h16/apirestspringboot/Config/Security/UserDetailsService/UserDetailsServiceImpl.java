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

public class UserDetailsServiceImpl implements UserDetailsService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;


    public UserDetailsServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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
