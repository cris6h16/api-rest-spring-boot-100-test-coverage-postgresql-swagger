package org.cris6h16.apirestspringboot.Config.Security.UserDetailsService;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
public class UserDetailsServiceImplIntegrationTest {


    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    Long id;

    @BeforeEach
    void setUp() {
        userService.deleteAll();
        id = userService.create(CreateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build());
    }

    @Test
    void UserDetailsServiceImplTest_loadUserByUsername() {
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(
                userRepository,
                passwordEncoder
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername("cris6h16");
        assertThat(userDetails)
                .isNotNull()
                .isInstanceOf(UserWithId.class)
                .extracting("id", "username")
                .containsExactly(id, "cris6h16");
    }
}
