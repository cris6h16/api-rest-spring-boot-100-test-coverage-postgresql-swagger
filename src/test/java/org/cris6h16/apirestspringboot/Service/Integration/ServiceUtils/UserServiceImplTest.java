package org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserServiceTraversalException;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TODO: doc: Any service layer should be tested as Integration, due that all are wrapped by {@link ServiceUtils#createATraversalExceptionHandled(Exception, boolean)}
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRepository.flush();
        roleRepository.flush();
    }

    /**
     * The unique verification on the service layer, due to I cannot verify
     * It in the entity layer, because the password is passed encrypted.
     */
    @Test
    @Tag("create")
    @Tag("UserServiceTransversalException")
    void UserService_create_ThrowsPasswordIsTooShortException() {
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("1234567")
                .email("cris6h16@gmail.com")
                .build();

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    // todo: make a diagram of Traversal exceptions and how they are used to pass by the layers
    @Test
    @Tag("UserServiceTransversalException")
    @Tag("create")
    void UserService_create_paramNull() {
        // Arrange
        CreateUpdateUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.DTO.NULL)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    @Test
    @Tag("UnhandledException")
    @Tag("create")
    void UserService_create_ThrowsUnhandledException_ThenGenericResponse() {
        // Arrange
        CreateUpdateUserDTO dto = new CreateUpdateUserDTO() {
            @Override
            public String getEmail() {
                throw new LazyInitializationException("cris6h16's Unhandled exception"); // random exception
            }
        };
        dto.setUsername("cris6h16");
        dto.setPassword("12345678");
        dto.setEmail("githubcomcris6h16@gmail.com");

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Test
    @Tag("create")
    void UserService_create_RoleNonexistentInDB() { // default
        // Arrange
        CreateUpdateUserDTO dto = createValidDTO();

        // Act
        Long id = userService.create(dto);

        // Assert
        RoleEntity role = roleRepository.findByName(ERole.ROLE_USER).orElse(null);
        UserEntity user = userRepository.findById(id).orElse(null);
        assertThat(role).isNotNull();
        assertThat(user).isNotNull();

        assertThat(user)
                .hasFieldOrPropertyWithValue("roles", Set.of(role))
                .hasFieldOrPropertyWithValue("username", dto.getUsername())
                .hasFieldOrPropertyWithValue("email", dto.getEmail());

        assertThat(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .isTrue();

        assertThat(role)
                .hasFieldOrPropertyWithValue("name", ERole.ROLE_USER)
                .hasNoNullFieldsOrPropertiesExcept("id");
    }


    @Test
    @Tag("create")
    void UserService_create_RoleExistentInDB() {
        // Arrange
        CreateUpdateUserDTO dto = createValidDTO();
        RoleEntity r = roleRepository.saveAndFlush(RoleEntity.builder().name(ERole.ROLE_USER).build());

        // Act
        Long id = userService.create(dto);

        // Assert
        UserEntity user = userRepository.findById(id).orElse(null);
        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("roles", Set.of(r))
                .hasFieldOrPropertyWithValue("username", dto.getUsername())
                .hasFieldOrPropertyWithValue("email", dto.getEmail());
        assertThat(passwordEncoder.matches(dto.getPassword(), user.getPassword())).isTrue();
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
    void UserService_get_UserNotFound() {
        // Arrange
        Long id = 1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }


    @Test
    @Tag("get")
    void UserService_get_UserIdInvalid_Negative() {
        // Arrange
        Long id = -1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get")
    void UserService_get_UserIdInvalid_Zero() {
        // Arrange
        Long id = 0L;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get")
    void UserService_get_UserIdInvalid_Null() {
        // Arrange
        Long id = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    @Test
    @Tag("get")
    void UserService_get_RolesNull() {
        // Arrange
        UserEntity usr = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(null)
                .build();

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

    @Test
    @Tag("get")
    @Tag("correct")
    void UserService_get_UserFoundWithRoles() {
        // Arrange
        Long id = 1L;
        UserEntity createdEntity = userRepository.saveAndFlush(UserEntity.builder()
                .id(id)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(Set.of(RoleEntity.builder().id(10L).name(ERole.ROLE_USER).build()))
                .build());

        // Act
        PublicUserDTO dto = userService.get(id);

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("username", createdEntity.getUsername())
                .hasFieldOrPropertyWithValue("email", createdEntity.getEmail())
                .hasFieldOrPropertyWithValue("roles", createdEntity.getRoles());
    }


    @Test
    @Tag("get")
    void UserService_get_UserFoundWithRolesNull() {
        // Arrange
        Long id = 1L;
        UserEntity createdEntity = userRepository.saveAndFlush(UserEntity.builder()
                .id(id)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(null)
                .build());

        // Act
        PublicUserDTO dto = userService.get(id);

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("username", createdEntity.getUsername())
                .hasFieldOrPropertyWithValue("email", createdEntity.getEmail())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>());

    }

    @Test
    @Tag("get")
    @Disabled
    void UserService_get_ThrowsUnhandledException() {
        // I couldn't implement this

        /* Arrange
        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
        verify(userRepository).findById(id); */
    }

    @Test
    @Tag("update")
    void UserService_update_UserNotFound() {
        // Arrange
        Long id = 1L;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
        verify(userRepository).findById(id);
    }


    @Test
    @Tag("update")
    void UserService_update_UserIdInvalid_Negative() {
        // Arrange
        Long id = -1L;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void UserService_update_UserIdInvalid_Zero() {
        // Arrange
        Long id = 0L;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void UserService_update_UserIdInvalid_Null() {
        // Arrange
        Long id = null;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void UserService_update_DTO_Null() {
        // Arrange
        Long id = 1L;
        CreateUpdateUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.DTO.NULL)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void UserService_update_DTO_WantUpdateUsername() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(Set.of(RoleEntity.builder().name(ERole.ROLE_USER).build()))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO updateUsernameDTO = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .build();

        // Act
        userService.update(original.getId(), updateUsernameDTO);
        original = userRepository.findById(original.getId()).orElse(null);

        // Assert
        assertThat(original)
                .isNotNull()
                .hasFieldOrPropertyWithValue("username", updateUsernameDTO.getUsername())
                .hasFieldOrPropertyWithValue("email", original.getEmail())
                .hasFieldOrPropertyWithValue("password", original.getPassword())
                .hasFieldOrPropertyWithValue("roles", original.getRoles());
    }

    @Test
    @Tag("update")
    void UserService_update_DTO_WantUpdateEmail() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(Set.of(RoleEntity.builder().name(ERole.ROLE_USER).build()))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO updateEmailDTO = CreateUpdateUserDTO.builder()
                .username("cris6h16@example.com")
                .build();

        // Act
        userService.update(original.getId(), updateEmailDTO);
        original = userRepository.findById(original.getId()).orElse(null);

        // Assert
        assertThat(original)
                .isNotNull()
                .hasFieldOrPropertyWithValue("username", original.getUsername())
                .hasFieldOrPropertyWithValue("email", updateEmailDTO.getEmail())
                .hasFieldOrPropertyWithValue("password", original.getPassword())
                .hasFieldOrPropertyWithValue("roles", original.getRoles());
    }


    @Test
    @Tag("update")
    void UserService_update_DTO_WantUpdatePasswordInvalid() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(Set.of(RoleEntity.builder().name(ERole.ROLE_USER).build()))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO updatePasswordDTO = CreateUpdateUserDTO.builder()
                .password("1234567")
                .build();

        // Act
        assertThatThrownBy(() -> userService.update(original.getId(), updatePasswordDTO))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void UserService_update_DTO_WantUpdatePassword() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(Set.of(RoleEntity.builder().name(ERole.ROLE_USER).build()))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO updatePasswordDTO = CreateUpdateUserDTO.builder()
                .password("12345678")
                .build();

        // Act
        userService.update(original.getId(), updatePasswordDTO);

        // Assert
        UserEntity updated = userRepository.findById(original.getId()).orElse(null);
        assertThat(updated)
                .isNotNull()
                .hasFieldOrPropertyWithValue("username", original.getUsername())
                .hasFieldOrPropertyWithValue("email", original.getEmail())
                .hasFieldOrPropertyWithValue("roles", original.getRoles())
                .hasFieldOrPropertyWithValue("password", updatePasswordDTO.getPassword());

    }

    @Test
    @Tag("update")
    void UserService_update_ThrowsUnhandledException_ThenGenericResponse() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(Set.of(RoleEntity.builder().name(ERole.ROLE_USER).build()))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO thrower = new CreateUpdateUserDTO() {
            @Override
            public String getPassword() {
                throw new ArrayIndexOutOfBoundsException(); // random exception
            }
        };

        // Act & Assert
        assertThatThrownBy(() -> userService.update(original.getId(), thrower))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Tag("delete")
    void UserService_delete_UserIdInvalid_Negative() {
        // Arrange
        Long id = -1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void UserService_delete_UserIdInvalid_Zero() {
        // Arrange
        Long id = 0L;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void UserService_delete_UserIdInvalid_Null() {
        // Arrange
        Long id = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void UserService_delete_UserNotFound() {
        // Arrange
        Long id = 1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
        verify(userRepository).findById(id);
    }

    @Test
    @Tag("delete")
    @Disabled()
    void UserService_delete_ThrowsUnhandledException() {
        // Arrange
        Long id = 1L;

        // I couldn't implement this

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
        verify(userRepository).findById(id);
    }


    @Test
    @Tag("get(pageable)")
    void UserService_get_Pageable_pageNumInvalid_Negative() {
        // Arrange
        int pageNum = -1;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "id"));

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageStartingWith("Page")
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get(pageable)")
    void UserService_get_Pageable_pageSizeInvalid_Negative() {
        // Arrange
        int pageNum = 1;
        int pageSize = -1;
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "id"));

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageStartingWith("Page")
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get(pageable)")
    void UserService_get_PageableNull_ThenGenericResponse() {
        // Arrange
        Pageable pageable = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageStartingWith(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get(pageable)")
    void UserService_get_Pageable_SortByInvalid_NonexistentAttribute_thenGenericFail() {
        // Arrange
        int pageNum = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "ttt"));

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))

                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageStartingWith("No property") // No property 'ttt' found for type 'UserEntity'
                .hasMessageEndingWith("found")
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get(pageable)")
    @Disabled
    void UserService_get_Pageable_UnhandledException() {
        // I couldn't implement this
    }



//
//    private void mockRoleRepository(boolean isPresent) {
//        if (isPresent) {
//            when(roleRepository.findByName(any(ERole.class)))
//                    .thenReturn(Optional.of(new RoleEntity(10L, ERole.ROLE_USER)));
//            return;
//        }
//        when(roleRepository.findByName(any(ERole.class)))
//                .thenReturn(Optional.empty());
//    }
//
//    private void mockPasswordEncoder() {
//        when(passwordEncoder.encode(any(CharSequence.class)))
//                .thenReturn("{bcrypt}$2a$1...");
//    }
//
//    private void mockUserRepositorySave() {
//        when(userRepository.save(any(UserEntity.class)))
//                .thenReturn(UserEntity.builder().id(1L).build());
//    }
//
//    private void verifyAllDependencies(CreateUpdateUserDTO dto, boolean roleRetrieved) {
//        verify(roleRepository).findByName(ERole.ROLE_USER);// verify if was called
//        verify(passwordEncoder).encode(dto.getPassword()); // verify if was called
//        verify(userRepository).save(argThat(user -> // verify if the user requested to save is the same as DTO
//                user != null &&
//                        user.getId() == null &&
//                        user.getUsername().equals(dto.getUsername()) &&
//                        user.getPassword().startsWith("{bcrypt}$") &&
//                        user.getEmail().equals(dto.getEmail()) &&
//                        user.getRoles().size() == 1 &&
//                        (roleRetrieved ? user.getRoles().stream().findFirst().get().getId() == 10L : true) &&
//                        user.getCreatedAt() != null
//        ));
//    }
}