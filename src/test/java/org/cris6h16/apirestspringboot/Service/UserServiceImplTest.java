package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
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
     * The unique verification on the service layer, due to I cannot verify
     * It in the entity layer, because the password is passed encrypted.
     */
    @Test
    @Tag("ResponseStatusException")
    @Tag("create")
    void UserService_create_ThrowsPasswordIsTooShortException() {
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("1234567")
                .email("cris6h16@gmail.com")
                .build();
        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(PasswordIsTooShortException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG);
    }

    @Test
    @Tag("create")
    void UserService_create_paramNull() {
        // Arrange
        CreateUpdateUserDTO dto = null;

        // Act & Assert
        userService.create(dto);


        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    @Tag("create")
    void UserService_create_RoleNonexistentInDB() {
        // Arrange
        CreateUpdateUserDTO dto = createValidDTO();
        mockRoleRepository(false);
        mockPasswordEncoder();
        mockUserRepositorySave();

        // Act
        Long id = userService.create(dto);

        // Assert
        assertThat(id).isNotNull().isEqualTo(1L);
        verifyAllDependencies(dto, false);
    }


    @Test
    @Tag("create")
    void UserService_create_AllSuccessfulReturnsId() {
        // Arrange
        CreateUpdateUserDTO dto = createValidDTO();
        mockRoleRepository(true);
        mockPasswordEncoder();
        mockUserRepositorySave();

        // Act
        Long id = userService.create(dto);

        // Assert
        assertThat(id).isNotNull().isEqualTo(1L);
        verifyAllDependencies(dto, true);
    }

    private CreateUpdateUserDTO createValidDTO() {
        return CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }

    private void mockRoleRepository(boolean isPresent) {
        if (isPresent) {
            when(roleRepository.findByName(any(ERole.class)))
                    .thenReturn(Optional.of(new RoleEntity(10L, ERole.ROLE_USER)));
            return;
        }
        when(roleRepository.findByName(any(ERole.class)))
                .thenReturn(Optional.empty());
    }

    private void mockPasswordEncoder() {
        when(passwordEncoder.encode(any(CharSequence.class)))
                .thenReturn("{bcrypt}$2a$1...");
    }

    private void mockUserRepositorySave() {
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(UserEntity.builder().id(1L).build());
    }

    private void verifyAllDependencies(CreateUpdateUserDTO dto, boolean roleRetrieved) {
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(passwordEncoder).encode(dto.getPassword());
        verify(userRepository).save(argThat(user -> // verify if the user requested to save is the same as DTO
                user != null &&
                        user.getId() == null &&
                        user.getUsername().equals(dto.getUsername()) &&
                        user.getPassword().startsWith("{bcrypt}$") &&
                        user.getEmail().equals(dto.getEmail()) &&
                        user.getRoles().size() == 1 &&
                        (roleRetrieved ? user.getRoles().stream().findFirst().get().getId() == 10L : true) &&
                        user.getCreatedAt() != null
        ));
    }


    @Test
    @Tag("get")
    void UserService_get_UserNotFound() {
        // Arrange
        Long id = 1L;
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(NotFoundException.User.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND);
    }

    @Test
    @Tag("get")
    void UserService_get_UserFoundWithRolesEmpty() {
        // Arrange
        UserEntity usr = createValidUserEntity(false, false);

        when(userRepository.findById(usr.getId()))
                .thenReturn(Optional.of(usr));

        // Act
        PublicUserDTO user = userService.get(usr.getId());

        // Assert //TODO: improve tests with this format
        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", usr.getId())
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("email", usr.getEmail())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>());


    }

    @Test
    @Tag("get")
    void UserService_get_UserFoundWithRolesNull() {
        // Arrange
        UserEntity usr = createValidUserEntity(false, true);

        when(userRepository.findById(usr.getId()))
                .thenReturn(Optional.of(usr));

        // Act
        PublicUserDTO user = userService.get(usr.getId());

        // Assert
        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", usr.getId())
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("email", usr.getEmail())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>());


    }

    UserEntity createValidUserEntity(boolean withRolesUser, boolean withRolesNull) {
        UserEntity.UserEntityBuilder builder = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a$1...")
                .roles(withRolesNull ? null : new HashSet<>(1));

        if (withRolesUser) {
            builder.roles(Set.of(RoleEntity.builder().id(10L).name(ERole.ROLE_USER).build()));
        }

        return builder.build();
    }
}