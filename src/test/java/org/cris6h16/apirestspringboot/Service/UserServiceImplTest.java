package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserServiceTraversalException;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    private ServiceUtils serviceUtils;


    @InjectMocks
    private UserServiceImpl userService;

    /**
     * The unique verification on the service layer, due to I cannot verify
     * It in the entity layer, because the password is passed encrypted.
     */
    @Test
    @Tag("create")
    @Tag("UserServiceTraversalException")
    void UserService_create_ThrowsPasswordIsTooShortException() {
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("1234567")
                .email("cris6h16@gmail.com")
                .build();

        when(serviceUtils.createATraversalExceptionHandled(any(), anyBoolean()))
                .thenReturn(new UserServiceTraversalException("Password is too short", HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    // todo: make a diagram of Traversal exceptions and how they are used to pass by the layers
    @Test
    @Tag("UserServiceTraversalException")
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
        CreateUpdateUserDTO dto = createValidDTO();
        mockRoleRepository(true);
        mockPasswordEncoder();
        when(userRepository.save(any(UserEntity.class)))
                .thenThrow(new RuntimeException("cris6h16's Unhandled exception"));

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
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
        verify(roleRepository).findByName(ERole.ROLE_USER);// verify if was called
        verify(passwordEncoder).encode(dto.getPassword()); // verify if was called
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
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
        verify(userRepository).findById(id);
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
        verify(userRepository).findById(usr.getId());
    }

    @Test
    @Tag("get")
    @Tag("correct")
    void UserService_get_UserFoundWithRoles() {
        // Arrange
        UserEntity usr = createValidUserEntity(true, false);

        when(userRepository.findById(usr.getId()))
                .thenReturn(Optional.of(usr));

        // Act
        PublicUserDTO user = userService.get(usr.getId());

        // Assert
        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", usr.getRoles().iterator().next().getId())
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("email", usr.getEmail())
                .hasFieldOrPropertyWithValue("roles", usr.getRoles().iterator().next());
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

    @Test
    @Tag("get")
    void UserService_get_ThrowsUnhandledException() {
        // Arrange
        Long id = 1L;
        when(userRepository.findById(id))
                .thenThrow(new RuntimeException("cris6h16's Unhandled exception"));

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
        verify(userRepository).findById(id);
    }

    @Test
    @Tag("update")
    void UserService_update_UserNotFound() {
        // Arrange
        Long id = 1L;
        CreateUpdateUserDTO dto = createValidDTO();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

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
        CreateUpdateUserDTO dto = createADTOForUpdate("username", "a");// (<attribute>,<value>) the rest is null
        UserEntity original = createValidUserEntity(true, false);

        when(userRepository.findById(original.getId()))
                .thenReturn(Optional.of(original));
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(null); // doesn't matter

        // Act
        userService.update(original.getId(), dto);

        // Assert
        verify(userRepository).findById(original.getId());
        verify(userRepository).save(argThat(user ->
                user != null &&
                        user.getId().equals(original.getId()) &&
                        user.getUsername().equals(dto.getUsername()) && // passed to update changed username
                        user.getEmail().equals(original.getEmail()) &&
                        user.getPassword().equals(original.getPassword()) &&
                        user.getRoles().equals(original.getRoles()) &&
                        user.getCreatedAt().equals(original.getCreatedAt()) &&
                        user.getUpdatedAt() != null
        ));


    }

    @Test
    @Tag("update")
    void UserService_update_DTO_WantUpdateEmail() {
        // Arrange
        CreateUpdateUserDTO dto = createADTOForUpdate("email", "cris6h16@example.com");// (<attribute>,<value>) the rest is null
        UserEntity original = createValidUserEntity(true, false);

        when(userRepository.findById(original.getId()))
                .thenReturn(Optional.of(original));
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(null); // doesn't matter

        // Act
        userService.update(original.getId(), dto);

        // Assert
        verify(userRepository).findById(original.getId());
        verify(userRepository).save(argThat(user ->
                user != null &&
                        user.getId().equals(original.getId()) &&
                        user.getUsername().equals(original.getEmail()) &&
                        user.getEmail().equals(dto.getEmail()) &&  // passed to update email changed
                        user.getPassword().equals(original.getPassword()) &&
                        user.getRoles().equals(original.getRoles()) &&
                        user.getCreatedAt().equals(original.getCreatedAt()) &&
                        user.getUpdatedAt() != null
        ));


    }


    @Test
    @Tag("update")
    void UserService_update_DTO_WantUpdatePasswordInvalid() {
        // Arrange
        CreateUpdateUserDTO dto = createADTOForUpdate("password", "7654321");// (<attribute>,<value>) the rest is null
        UserEntity original = createValidUserEntity(true, false);

        when(userRepository.findById(original.getId()))
                .thenReturn(Optional.of(original));

        // Act
        assertThatThrownBy(() -> userService.update(original.getId(), dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void UserService_update_DTO_WantUpdatePassword() {
        // Arrange
        CreateUpdateUserDTO dto = createADTOForUpdate("password", "87654321");// (<attribute>,<value>) the rest is null
        UserEntity original = createValidUserEntity(true, false);

        when(userRepository.findById(original.getId()))
                .thenReturn(Optional.of(original));
        mockPasswordEncoder();
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(null); // doesn't matter

        // Act
        userService.update(original.getId(), dto);

        // Assert
        verify(userRepository).findById(original.getId());
        verify(userRepository).save(argThat(user ->
                user != null &&
                        user.getId().equals(original.getId()) &&
                        user.getUsername().equals(original.getUsername()) &&
                        user.getEmail().equals(original.getEmail()) &&
                        user.getPassword().startsWith("{bcrypt}") && // passed to update password changed
                        user.getRoles().equals(original.getRoles()) &&
                        user.getCreatedAt().equals(original.getCreatedAt()) &&
                        user.getUpdatedAt() != null
        ));
    }

    @Test
    @Tag("update")
    void UserService_update_ThrowsUnhandledException_ThenGenericResponse() {
        // Arrange
        Long id = 1L;
        CreateUpdateUserDTO dto = createValidDTO();

        when(userRepository.findById(id))
                .thenThrow(new RuntimeException("cris6h16's Unhandled exception"));

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
        verify(userRepository).findById(id);
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
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
        verify(userRepository).findById(id);
    }

    @Test
    @Tag("delete")
    void UserService_delete_ThrowsUnhandledException() {
        // Arrange
        Long id = 1L;
        when(userRepository.findById(id))
                .thenThrow(new ArithmeticException("random cris6h16's Unhandled exception"));

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
    void UserService_get_PageableNull() {
        // Arrange
        int pageNum = 1;
        int pageSize = 1;
        Pageable pageable = null;

        try {
            // Act
            userService.get(pageable);
        } catch (IllegalArgumentException e) {
            // Assert
            assertThat(e.getMessage()).contains("Pageable must not be null");
        }

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessageStartingWith("")
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get(pageable)")
        void UserService_get_Pageable_SortByInvalid_NonexistentAttribute_thenGenericFail(){
        // Arrange
        int pageNum = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "ttt"));

        when(userRepository.findAll(pageable))
                .thenReturn(createValidUserEntities(10));
        try {
            userService.get(pageable);
        }catch (Exception e) {
            System.out.println(e);
        }

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessage(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get(pageable)")
        void UserService_get_Pageable_UnhandledException(){
        // Arrange
        int pageNum = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "id"));

        when(userRepository.findAll(pageable))
                .thenThrow(new RuntimeException("cris6h16's Unhandled exception"));

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTraversalException.class)
                .hasMessage(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    CreateUpdateUserDTO createADTOForUpdate(String attributeThatYouWantToUpdate, String val) {
        List<String> attributes = Arrays.asList("username", "email", "password");

        if (!attributes.contains(attributeThatYouWantToUpdate)) {
            throw new IllegalArgumentException("Invalid attribute for update");
        }

        CreateUpdateUserDTO.CreateUpdateUserDTOBuilder usr = CreateUpdateUserDTO.builder();
        usr = switch (attributeThatYouWantToUpdate) {
            case "username" -> usr.username(val);
            case "email" -> usr.email(val);
            case "password" -> usr.password(val);
            default -> usr; // should never reach here
        };

        return usr.build();
    }


    Page<UserEntity> createValidUserEntities(int n) {
        List<UserEntity> users = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            users.add(UserEntity.builder()
                    .id((long) i)
                    .username("cris6h16" + i)
                    .email("cris6h16" + i + "@gmail.com")
                    .password("{bcrypt}$2a81..." + i)
                    .roles(Set.of(RoleEntity.builder().id(10L).name(ERole.ROLE_USER).build()))
                    .build());
        }
        return new PageImpl<>(users, PageRequest.of(0, 10), n); //todo: add sort by what
    }

    UserEntity createValidUserEntity(boolean withRolesUser, boolean withRolesNull) {
        UserEntity.UserEntityBuilder builder = UserEntity.builder()
                .id(1L)
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.comm")
                .password("{bcrypt}$2a81...")
                .roles(withRolesNull ? null : new HashSet<>(1));

        if (withRolesUser) {
            builder.roles(Set.of(RoleEntity.builder().id(10L).name(ERole.ROLE_USER).build()));
        }


        UserEntity usr = builder.build();
        if (withRolesUser) {
            assertThat(usr.getRoles().iterator().next().getName().equals(ERole.ROLE_USER));
        }
        if (withRolesNull) {
            assertThat(usr.getRoles()).isNull();
        }

        return builder.build();
    }
}