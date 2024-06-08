package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


/**
 * Test class for {@link UserServiceImpl}, here I just test when
 * the test is successful, due to all methods in the mentioned
 * service are wrapped in a try-catch block, in the catch
 * we delegate a creation of {@link UserServiceTransversalException}
 * to {@link ServiceUtils}, then any exception threw in the methods of
 * {@link UserServiceImpl} should be tested as integration with {@link ServiceUtils}.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @implNote {@link UserServiceImpl} are tested in isolation, mocking the dependencies.
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
    private ServiceUtils serviceUtils;
    @InjectMocks
    private UserServiceImpl userService;


    /**
     * Test for {@link UserServiceImpl#create(CreateUpdateUserDTO)} when is successful.
     * <br>
     * Test: Create a user with a {@link ERole#ROLE_USER} role, the role is not found in the database,
     * then the role is created ( {@code id==null} ) and assigned to the user.
     * after the user is persisted in the database also in cascade the role.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("create")
    void ServiceUtils_create_RoleNonexistentInDBCascade_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId();
        CreateUpdateUserDTO dtoToCreate = createValidDTO();

        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(user);

        // Act
        Long id = userService.create(dtoToCreate);

        // Assert
        assertThat(id).isEqualTo(user.getId());
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals(dtoToCreate.getUsername()) &&
                        passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                        passedToDb.getPassword().startsWith("{bcrypt}$") &&
                        passedToDb.getRoles().size() == 1 &&
                        passedToDb.getRoles().iterator().next().getName().equals(ERole.ROLE_USER)));
    }

    /**
     * Create a {@link UserEntity} with an id, and containing a {@link RoleEntity} with an id.
     * <br>
     * It'll be used in the tests to simulate a user with a role persisted in the database..
     * <br>
     * The role is {@link ERole#ROLE_USER}
     *
     * @return {@link UserEntity} with an id, and containing a {@link RoleEntity} with an id.
     * @implNote The role is not persisted in the database, it's just a mock.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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
     * Test {@link UserServiceImpl#create(CreateUpdateUserDTO)} when is successful.
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
    void ServiceUtils_create_RoleExistentInDBThenAssignIt_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId();
        RoleEntity role = user.getRoles().iterator().next();

        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.ofNullable(role));
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenReturn(user);

        CreateUpdateUserDTO dtoToCreate = createValidDTO();

        // Act
        Long id = userService.create(dtoToCreate);

        // Assert
        assertThat(id).isEqualTo(user.getId());
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals(
                        dtoToCreate.getUsername()) &&
                        passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                        passedToDb.getPassword().startsWith("{bcrypt}$") &&
                        passedToDb.getRoles().size() == 1 &&
                        passedToDb.getCreatedAt() != null &&
                        passedToDb.getRoles().iterator().next().getId().equals(role.getId())));

    }

    /**
     * Create a {@link CreateUpdateUserDTO} with valid data.
     *
     * @return {@link CreateUpdateUserDTO}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private CreateUpdateUserDTO createValidDTO() {
        return CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }

    /**
     * Test {@link UserServiceImpl#get(Long)} when is successful.
     * <br>
     * Test: Get a user by id, the user is found in DB with {@code roles==null}
     * then the roles in the response should be an empty set.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("get")
    void ServiceUtils_get_UserFoundWithRolesNull_thenInRolesReturnEmptySet_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        entity.setRoles(null);

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        doNothing().when(serviceUtils).validateId(any(Long.class));

        // Act
        PublicUserDTO dto = userService.get(entity.getId());

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", entity.getId())
                .hasFieldOrPropertyWithValue("username", entity.getUsername())
                .hasFieldOrPropertyWithValue("email", entity.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", entity.getCreatedAt())
                .hasFieldOrPropertyWithValue("updatedAt", entity.getUpdatedAt())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>(0));

    }

    /**
     * Test {@link UserServiceImpl#get(Long)} when is successful.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("get")
    void ServiceUtils_get_UserFoundWithRoles_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        PublicUserDTO dto = userService.get(entity.getId());

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", entity.getId())
                .hasFieldOrPropertyWithValue("username", entity.getUsername())
                .hasFieldOrPropertyWithValue("email", entity.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", entity.getCreatedAt())
                .hasFieldOrPropertyWithValue("updatedAt", entity.getUpdatedAt())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>(Collections.singleton(new RoleDTO(ERole.ROLE_USER))));

    }


    /**
     * Test {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)} when is successful.
     * <br>
     * Test: Used in PATCH; We want update just the {@code username}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdateUsername_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16" + "helloword")
                .build();
        UserEntity updated = createUserEntityWithIdAndRolesWithId();
        updated.setUsername(dto.getUsername());

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        doNothing().when(serviceUtils).validateId(any(Long.class));
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(updated);

        // Act
        userService.update(entity.getId(), dto);

        // Assert
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getId().equals(entity.getId()) &&
                        passedToDb.getUsername().equals(dto.getUsername()) &&
                        passedToDb.getEmail().equals(entity.getEmail()) &&
                        passedToDb.getPassword().equals(entity.getPassword()) &&
                        passedToDb.getRoles().equals(entity.getRoles()) &&
                        passedToDb.getCreatedAt().equals(entity.getCreatedAt()) &&
                        passedToDb.getUpdatedAt() != null));
    }

    /**
     * Test {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)} when is successful.
     * <br>
     * Test: Used in PATCH; We want update just the {@code email}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdateEmail_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .email("helloword" + "cristianmherrera21@gmail.com")
                .build();
        UserEntity updated = createUserEntityWithIdAndRolesWithId();
        updated.setEmail(dto.getEmail());

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        doNothing().when(serviceUtils).validateId(any(Long.class));
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(updated);

        // Act
        userService.update(entity.getId(), dto);

        // Assert
        verify(userRepository).findById(entity.getId());
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getId().equals(entity.getId()) &&
                        passedToDb.getUsername().equals(entity.getUsername()) &&
                        passedToDb.getEmail().equals(dto.getEmail()) &&
                        passedToDb.getPassword().equals(entity.getPassword()) &&
                        passedToDb.getRoles().equals(entity.getRoles()) &&
                        passedToDb.getCreatedAt().equals(entity.getCreatedAt()) &&
                        passedToDb.getUpdatedAt() != null));
    }

    /**
     * Test {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)} when is successful.
     * <br>
     * Test: Used in PATCH; We want update just the {@code password}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdatePassword_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .password("12345678")
                .build();
        UserEntity updated = createUserEntityWithIdAndRolesWithId();
        updated.setPassword("{bcrypt}$2a81...");

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(passwordEncoder.encode(any(String.class))).thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(updated);

// Act
        userService.update(entity.getId(), dto);

// Assert
        verify(userRepository).findById(entity.getId());
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getId().equals(entity.getId()) &&
                        passedToDb.getUsername().equals(entity.getUsername()) &&
                        passedToDb.getEmail().equals(entity.getEmail()) &&
                        passedToDb.getPassword().startsWith("{bcrypt}$") &&
                        passedToDb.getRoles().equals(entity.getRoles()) &&
                        passedToDb.getCreatedAt().equals(entity.getCreatedAt()) &&
                        passedToDb.getUpdatedAt() != null));
    }

    /**
     * Test {@link UserServiceImpl#delete(Long)} when is successful.
     * <br>
     * Test: delete a user
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("delete")
    void ServiceUtils_delete_UserFound_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        doNothing().when(serviceUtils).validateId(any(Long.class));
        doNothing().when(userRepository).delete(entity);

        // Act
        userService.delete(entity.getId());

        // Assert
        verify(userRepository).delete(argThat(passedToDb ->
                passedToDb.getId().equals(entity.getId())));

    }

    /**
     * Test {@link UserServiceImpl#get(Pageable)} when is successful.
     * <br>
     * Test: get a page of notes
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("get(pageable)")
    void ServiceUtils_getPageable_ReturnList_Successful() {
        // Arrange
        int amount = 10;
        List<UserEntity> entities = getUserEntities(amount);
        Pageable pag = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.ASC, "id")
        );

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities));

        // Act
        List<PublicUserDTO> dtos = userService.get(pag);

        // Assert
        for (int i = 0; i < entities.size(); i++) {
            assertThat(dtos.get(i))
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("id", entities.get(i).getId())
                    .hasFieldOrPropertyWithValue("username", entities.get(i).getUsername())
                    .hasFieldOrPropertyWithValue("email", entities.get(i).getEmail())
                    .hasFieldOrPropertyWithValue("createdAt", entities.get(i).getCreatedAt())
                    .hasFieldOrPropertyWithValue("updatedAt", entities.get(i).getUpdatedAt())
                    .hasFieldOrPropertyWithValue("roles", new HashSet<>(Collections.singleton(new RoleDTO(ERole.ROLE_USER))));
        }

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
