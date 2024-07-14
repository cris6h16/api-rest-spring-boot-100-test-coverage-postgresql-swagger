package org.cris6h16.apirestspringboot.Services;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.EmailAlreadyExistException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.PasswordTooShortException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UsernameAlreadyExistsException;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


/**
 * Test class for {@link UserServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Validator validator;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    @Tag("create")
    void create_RoleNonexistentInDB_ThenCreateBoth_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId();
        CreateUserDTO dtoToCreate = createValidDTO();

        when(validator.validate(any(CreateUserDTO.class))).thenReturn(Collections.emptySet());
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(user);

        // Act
        Long id = userService.create(dtoToCreate);

        // Assert
        assertThat(id).isEqualTo(user.getId());
        verify(validator).validate(argThat(dto -> {
            CreateUserDTO dtoCasted = (CreateUserDTO) dto;
            return dtoCasted.getUsername().equals(dtoToCreate.getUsername()) &&
                    dtoCasted.getEmail().equals(dtoToCreate.getEmail()) &&
                    dtoCasted.getPassword().equals(dtoToCreate.getPassword());
        }));
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(passwordEncoder).encode(user.getPassword());
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals(dtoToCreate.getUsername()) &&
                        passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                        passedToDb.getPassword().equals("{bcrypt}$2a81...") &&
                        passedToDb.getRoles().size() == 1 &&
                        passedToDb.getRoles().iterator().next().getName().equals(ERole.ROLE_USER)));
    }

    @Test
    @Tag("create")
    void create_nullDTO_ThenIllegalArgumentException() {
        // Arrange
        CreateUserDTO dtoToCreate = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate))
                .isInstanceOf(IllegalArgumentException.class); // unhandled
    }


    @Test
    @Tag("create")
    void create_violationConstraintInDTO_ConstraintViolationException() {
        // Arrange
        CreateUserDTO dtoToCreate = mock(CreateUserDTO.class);
        Set<ConstraintViolation<CreateUserDTO>> violations = mock(Set.class);
        when(violations.isEmpty()).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate))
                .isInstanceOf(ConstraintViolationException.class)
                .isEqualTo(violations);

        verify(userRepository, never()).saveAndFlush(any());
    }

    // the unique validation in the service due that it leave the layer encrypted
    @Test
    @Tag("create")
    void create_passwordLengthLessThan8_ThenPasswordTooShortException() {
        // Arrange
        CreateUserDTO dtoToCreate = mock(CreateUserDTO.class);

        when(dtoToCreate.getPassword().length())
                .thenReturn(7);
        when(validator.validate(any(CreateUserDTO.class)))
                .thenReturn(Collections.emptySet());

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate))
                .isInstanceOf(PasswordTooShortException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("status", 400);

        verify(userRepository, never()).saveAndFlush(any());
    }


    @Test
    @Tag("create")
    void create_TrimFields() {
        // Arrange
        CreateUserDTO dtoToCreate = CreateUserDTO.builder()
                .username("  cris6h16  ")
                .password("  12345678  ")
                .email("    cristianmherrera21@gmail.com ")
                .build();

        when(validator.validate(any(CreateUserDTO.class)))
                .thenReturn(Collections.emptySet());
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenReturn(createUserEntityWithIdAndRolesWithId());

        // Act
        userService.create(dtoToCreate);

        // Assert
        verify(passwordEncoder).encode("12345678");
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals("cris6h16") &&
                        passedToDb.getEmail().equals("cristianmherrera21@gmail.com") &&
                        passedToDb.getPassword().equals("{bcrypt}$2a81..."))
        );
    }


    private UserEntity createUserEntityWithIdAndRolesWithId() {
        return UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().id(1L).name(ERole.ROLE_USER).build())))
                .createdAt(new Date())
                .build();
    }


    /**
     * Test {@link UserServiceImpl#create(CreateUserDTO)} when is successful.
     * <br>
     * Test: Create a user with a {@link ERole#ROLE_USER} role, the role is found in the database,
     * then the role is assigned to the user.
     * after the user is persisted in the database.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("create")
    void create_RoleExistentInDBThenAssignIt_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId();
        RoleEntity role = user.getRoles().iterator().next();

        when(validator.validate(any(CreateUserDTO.class)))
                .thenReturn(Collections.emptySet());
        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.ofNullable(role));
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenReturn(user);

        CreateUserDTO dtoToCreate = createValidDTO();

        // Act
        Long id = userService.create(dtoToCreate);

        // Assert
        assertThat(id).isEqualTo(user.getId());
        verify(validator).validate(dtoToCreate);
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(passwordEncoder).encode(user.getPassword());
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals(dtoToCreate.getUsername()) &&
                        passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                        passedToDb.getPassword().equals("{bcrypt}$2a81...") &&
                        passedToDb.getRoles().size() == 1 &&
                        passedToDb.getCreatedAt() != null &&
                        passedToDb.getUpdatedAt() == null &&
                        passedToDb.getRoles().iterator().next().getId().equals(role.getId()) &&
                        passedToDb.getRoles().iterator().next().getName().equals(role.getName())
        ));
    }

    /**
     * Create a {@link CreateUserDTO} with valid data.
     *
     * @return {@link CreateUserDTO}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private CreateUserDTO createValidDTO() {
        return CreateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }


    /**
     * Test {@link UserServiceImpl#getById(Long)} when is successful.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("getById")
    void getById_UserFoundWithRoles_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        Set<PublicRoleDTO> rolesOwned = entity.getRoles().stream()
                .map(role -> new PublicRoleDTO(role.getName()))
                .collect(Collectors.toSet());

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        PublicUserDTO dto = userService.getById(entity.getId());

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", entity.getId())
                .hasFieldOrPropertyWithValue("username", entity.getUsername())
                .hasFieldOrPropertyWithValue("email", entity.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", entity.getCreatedAt())
                .hasFieldOrPropertyWithValue("updatedAt", entity.getUpdatedAt())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>(rolesOwned));
        verify(userRepository).findById(entity.getId());
    }

    @Test
    @Tag("getById")
    void getById_nullId_ThenIllegalArgumentException() {
        // Arrange
        Long id = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @Tag("getById")
    void getById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        Optional<UserEntity> entity = mock(Optional.class);

        when(entity.isPresent()).thenReturn(false);
        when(userRepository.findById(id)).thenReturn(entity);

        // Act & Assert
        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", 404);

        verify(userRepository).findById(id);
    }

    /**
     * Test {@link UserServiceImpl#getById(Long)} when is successful.
     * <br>
     * Test: Get a user by id, the user is found in DB with {@code roles==null}
     * then the roles in the response should be an empty set.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("getById")
    void getById_UserFoundWithRolesNull_thenInRolesReturnEmptySet_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        entity.setRoles(null);

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        PublicUserDTO dto = userService.getById(entity.getId());

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", entity.getId())
                .hasFieldOrPropertyWithValue("username", entity.getUsername())
                .hasFieldOrPropertyWithValue("email", entity.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", entity.getCreatedAt())
                .hasFieldOrPropertyWithValue("updatedAt", entity.getUpdatedAt())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>(0));
        verify(userRepository).findById(entity.getId());
    }


    /**
     * Test {@link UserServiceImpl#deleteById(Long)} when is successful.
     * <br>
     * Test: deleteById a user
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("deleteById")
    void deleteById_Successful() {
        // Arrange
        Long id = 1L;
        doNothing().when(userRepository).deleteById(id);

        // Act
        userService.deleteById(id);

        // Assert
        verify(userRepository).deleteById(id);
    }

    @Test
    @Tag("deleteById")
    void deleteById_nullId_ThenIllegalArgumentException() {
        // Arrange
        Long id = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteById(id))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @Tag("deleteById")
    void deleteById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        Optional<UserEntity> entity = mock(Optional.class);

        when(entity.isPresent()).thenReturn(false);
        when(userRepository.findById(id)).thenReturn(entity);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", 404);
        verify(userRepository).findById(id);
    }


    @Test
    @Tag("getPage")
    void getPage_ReturnList_Successful() {
        // Arrange
        int amount = 10;
        List<UserEntity> entities = getUserEntities(amount);
        Pageable pag = mock(Pageable.class);

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities));

        // Act
        List<PublicUserDTO> list = userService.getPage(pag);

        // Assert
        for (int i = 0; i < entities.size(); i++) {
            assertThat(list.get(i))
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("id", entities.get(i).getId())
                    .hasFieldOrPropertyWithValue("username", entities.get(i).getUsername())
                    .hasFieldOrPropertyWithValue("email", entities.get(i).getEmail())
                    .hasFieldOrPropertyWithValue("createdAt", entities.get(i).getCreatedAt())
                    .hasFieldOrPropertyWithValue("updatedAt", entities.get(i).getUpdatedAt())
                    .hasFieldOrPropertyWithValue("roles", new HashSet<>(Collections.singleton(new PublicRoleDTO(ERole.ROLE_USER))));
        }
        verify(userRepository).findAll(pag);
    }

    @Test
    @Tag("getPage")
    void getPage_nullPageable_ThenIllegalArgumentException() {
        // Arrange
        Pageable pag = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.getPage(pag))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).findAll(any(Pageable.class));
    }


    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_Successful() {
        // Arrange
        Long id = 1L;
        String newUsername = "newUsername";
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO(newUsername);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        doNothing().when(userRepository).updateUsernameById(newUsername, id);

        // Act
        userService.patchUsernameById(id, dto);

        // Assert
        verify(userRepository).existsById(id);
        verify(userRepository).existsByUsername(newUsername);
        verify(userRepository).updateUsernameById(newUsername, id);
    }

    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_DTONull_ThenIllegalArgumentException() {
        // Arrange
        Long id = 1L;
        PatchUsernameUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }

    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_violatesConstraints_ThenConstraintViolationException() {
        // Arrange
        Long id = 1L;
        PatchUsernameUserDTO dto = mock(PatchUsernameUserDTO.class);
        Set<ConstraintViolation<PatchUsernameUserDTO>> violations = mock(Set.class);

        when(violations.isEmpty()).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(ConstraintViolationException.class)
                .isEqualTo(violations);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }

    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_TrimFields() {
        // Arrange
        Long id = 1L;
        String newUsername = "  newUsername    ";
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO(newUsername);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        doNothing().when(userRepository).updateUsernameById(newUsername, id);

        // Act
        userService.patchUsernameById(id, dto);

        // Assert
        verify(userRepository).updateUsernameById("newUsername", id);
    }


    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_nullId_ThenIllegalArgumentException() {
        // Arrange
        Long id = null;
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO("newUsername");

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }

    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO("newUsername");

        when(userRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", 404);
        verify(userRepository).existsById(id);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }


    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_UsernameAlreadyExists_ThenUsernameAlreadyExistsException() {
        // Arrange
        Long id = 1L;
        String newUsername = "newUsername";
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO(newUsername);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByUsername(newUsername)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining(Cons.User.Constrains.USERNAME_UNIQUE_MSG)
                .hasFieldOrPropertyWithValue("status", 409);
        verify(userRepository).existsById(id);
        verify(userRepository).existsByUsername(newUsername);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }


    @Test
    @Tag("patchEmailById")
    void patchEmailById_Successful() {
        // Arrange
        Long id = 1L;
        String newEmail = "cristianmherrera21@gmail.com";
        PatchEmailUserDTO dto = new PatchEmailUserDTO(newEmail);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        doNothing().when(userRepository).updateEmailById(newEmail, id);

        // Act
        userService.patchEmailById(id, dto);

        // Assert
        verify(userRepository).existsById(id);
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository).updateEmailById(newEmail, id);
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_DTONull_ThenIllegalArgumentException() {
        // Arrange
        Long id = 1L;
        PatchEmailUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_violatesConstraints_ThenConstraintViolationException() {
        // Arrange
        Long id = 1L;
        PatchEmailUserDTO dto = mock(PatchEmailUserDTO.class);
        Set<ConstraintViolation<PatchEmailUserDTO>> violations = mock(Set.class);

        when(violations.isEmpty()).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(ConstraintViolationException.class)
                .isEqualTo(violations);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_TrimFields() {
        // Arrange
        Long id = 1L;
        String newEmail = "  cristianmherrera21@gmail.com  ";
        PatchEmailUserDTO dto = new PatchEmailUserDTO(newEmail);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        doNothing().when(userRepository).updateEmailById(newEmail, id);

        // Act
        userService.patchEmailById(id, dto);

        // Assert
        verify(userRepository).updateEmailById("cristianmherrera21@gmail.com", id);
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_nullId_ThenIlegalArgumentException() {
        // Arrange
        Long id = null;
        PatchEmailUserDTO dto = new PatchEmailUserDTO("cristianmherrera21@gmail.com");

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        PatchEmailUserDTO dto = new PatchEmailUserDTO("cristianmherrera21@gmail.com");

        when(userRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", 404);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_EmailAlreadyExists_ThenEmailAlreadyExistException() {
        // Arrange
        Long id = 1L;
        String newEmail = "cristianmherrera21@gmail.com";
        PatchEmailUserDTO dto = new PatchEmailUserDTO(newEmail);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(EmailAlreadyExistException.class)
                .hasMessageContaining(Cons.User.Constrains.EMAIL_UNIQUE_MSG)
                .hasFieldOrPropertyWithValue("status", 409);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("deleteAll")
    void deleteAll_Successful() {
        doNothing().when(userRepository).deleteAll();
        userService.deleteAll();
        verify(userRepository).deleteAll();
    }

    @Test
    @Tag("createAdmin")
    void createAdmin_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId();
        CreateUserDTO dtoToCreate = createValidDTO();

        when(validator.validate(any(CreateUserDTO.class))).thenReturn(Collections.emptySet());
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(user);

        // Act
        Long id = userService.createAdmin(dtoToCreate);

        // Assert
        assertThat(id).isEqualTo(user.getId());
        verify(validator).validate(argThat(dto -> {
            CreateUserDTO dtoCasted = (CreateUserDTO) dto;
            return dtoCasted.getUsername().equals(dtoToCreate.getUsername()) &&
                    dtoCasted.getEmail().equals(dtoToCreate.getEmail()) &&
                    dtoCasted.getPassword().equals(dtoToCreate.getPassword());
        }));
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
        verify(passwordEncoder).encode(user.getPassword());
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals(dtoToCreate.getUsername()) &&
                        passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                        passedToDb.getPassword().equals("{bcrypt}$2a81...") &&
                        passedToDb.getRoles().size() == 1 &&
                        passedToDb.getRoles().iterator().next().getName().equals(ERole.ROLE_ADMIN)));
    }


    /**
     * Create a list of {@link UserEntity} with roles, for simulate
     * a page of persisted users with their roles.
     *
     * @param amount amount of users to create
     * @return list of {@link UserEntity} with roles
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private List<UserEntity> getUserEntities(int amount) {
        List<UserEntity> entities = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            entities.add(UserEntity.builder()
                    .id(i)
                    .username("cris6h16" + i)
                    .email(i + "cristianmherrera21@gmail.com")
                    .password("{bcrypt}$2a81..." + i)
                    .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().id(i).name(ERole.ROLE_USER).build())))
                    .createdAt(new Date())
                    .build());
        }
        return entities;

    }
}
