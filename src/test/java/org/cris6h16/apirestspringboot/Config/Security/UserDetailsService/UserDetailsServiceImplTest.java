package org.cris6h16.apirestspringboot.Config.Security.UserDetailsService;

import org.cris6h16.apirestspringboot.Config.Security.UserDetailsService.UserDetailsServiceImpl;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.Security.UserDetailsService.UserHasNotRolesException;

/**
 * Test class for {@link UserDetailsServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a> where
 * @see UserDetailsServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)}
     * when the user is not found then It should throw a {@link UsernameNotFoundException}
     * with the message {@link Cons.User.Fails#NOT_FOUND} and the status {@link HttpStatus#NOT_FOUND}
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @see UserDetailsServiceImpl#loadUserByUsername(String)
     */
    @Test
    void UserDetailsServiceImplTest_loadUserByUsername_UsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("username"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(Cons.User.Fails.NOT_FOUND);
    }

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)} when the user is found
     * but the roles are null then It should throw an exception extended of
     * {@link AbstractExceptionWithStatus}, in this case it'll be {@link UserHasNotRolesException}<br>
     * I want to emphasize that this fail will occur if a user was created externally to the application
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @see UserDetailsServiceImpl#loadUserByUsername(String)
     * @see UserHasNotRolesException
     */
    @Test
    void UserDetailsServiceImplTest_UserFoundWithRolesNull() {
        // Arrange
        UserEntity usr = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .roles(null)
                .build();

        when(userRepository.findByUsername("username"))
                .thenReturn(Optional.of(usr));

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("username"))
                .isInstanceOf(UserHasNotRolesException.class)
                .hasMessage(Cons.User.UserDetailsServiceImpl.USER_HAS_NOT_ROLES)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.FORBIDDEN);
    }

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)} when the user is found
     * but the roles are empty then It should return a {@link UserDetails} with the roles empty
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @see UserDetailsServiceImpl#loadUserByUsername(String)
     * @see UserDetails
     */
    @Test
    void UserDetailsServiceImplTest_UserFoundWithRolesEmpty() {
        // Arrange
        UserEntity usr = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("cris6h16"))
                .thenReturn(Optional.of(usr));

        // Act & Assert
        UserDetails userDetails = userDetailsService.loadUserByUsername("cris6h16");
        assertThat(userDetails)
                .isNotNull()
                .isInstanceOf(UserDetails.class)
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("password", usr.getPassword());
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)} when the user is found
     * and the roles are not empty then It should return a {@link UserDetails} with the roles
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @see UserDetailsServiceImpl#loadUserByUsername(String)
     * @see UserDetails
     */
    @Test
    void UserDetailsServiceImplTest_UserFound_Successful() {
        // Arrange
        Set<RoleEntity> roles = Collections.singleton(RoleEntity.builder()
                .id(1L)
                .name(ERole.ROLE_USER)
                .build());

        UserEntity usr = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .roles(roles)
                .build();

        when(userRepository.findByUsername(usr.getUsername()))
                .thenReturn(Optional.of(usr));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(usr.getUsername());

        // Assert
        assertThat(userDetails)
                .isNotNull()
                .isInstanceOf(UserDetails.class)
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("password", usr.getPassword());
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                .isEqualTo(usr.getRoles().iterator().next().getName().name());
    }

    // userDetails.getAuthorities().iterator().next().getAuthority()

}