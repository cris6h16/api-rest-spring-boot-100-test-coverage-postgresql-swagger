package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.PreExceptions.PasswordIsTooShortException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    /**
     * Test of create method, of class {@link UserService#create(CreateUpdateUserDTO)}
     */
    @Test
    void UserService_create_returnId() {
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();

        when(roleRepository.findByName(any(ERole.class)))
                .thenReturn(Optional.of(new RoleEntity(10L, ERole.ROLE_USER)));
        when(passwordEncoder.encode(any(CharSequence.class)))
                .thenReturn("{bcrypt}$2a$1...");
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(UserEntity.builder().id(1L).build());

        Long id = userService.create(dto);

        assertThat(id).isNotNull();
        assertThat(id).isEqualTo(1L);
    }

    /**
     * The unique verification on the service layer, due to I cannot verify
     * It in the entity layer, because the password is passed encrypted.
     */
    @Test
    void UserService_create_ThrowsPasswordIsTooShortException() {
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("1234567")
                .email("cris6h16@gmail.com")
                .build();
        Assertions.assertThrows(PasswordIsTooShortException.class,
                () -> userService.create(dto));
    }

}