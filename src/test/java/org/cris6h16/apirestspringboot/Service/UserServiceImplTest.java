package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


//@SpringBootTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
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

    private CreateUpdateUserDTO createValidDTO() {
        return CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }

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

    @Test
    @Tag("get")
    @Tag("correct")
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

    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdateEmail_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .email("helloword"+"cristianmherrera21@gmail.com")
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

    }}
