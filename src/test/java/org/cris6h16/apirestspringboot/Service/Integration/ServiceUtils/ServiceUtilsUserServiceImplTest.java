package org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * // todo: doc: the difficult of mock the database exceptions & its messages, due that the exception  handled work if has the exact exception with the exact message otherwise the response should be a generic thats the reason why i decided not mock the database
 * // todo: doc: this should be tested when all test in database layer are done
 * TODO: doc: Any service layer should be tested as Integration, due that all are wrapped by {@link ServiceUtils#createATraversalExceptionHandled(Exception, boolean)}
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
class ServiceUtilsUserServiceImplTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private NoteRepository noteRepository;

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
    void ServiceUtils_create_ThrowsPasswordIsTooShortException() {
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("1234567")
                .email("cris6h16@gmail.com")
                .build();

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    // todo: make a diagram of Traversal exceptions and how they are used to pass by the layers
    @Test
    @Tag("UserServiceTransversalException")
    @Tag("create")
    void ServiceUtils_create_paramNull() {
        // Arrange
        CreateUpdateUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.DTO.NULL)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    @Test
    @Tag("UnhandledException")
    @Tag("create")
    void ServiceUtils_create_ThrowsUnhandledException_ThenGenericResponse() {
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
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Tag("get")
    void ServiceUtils_get_UserNotFound() {
        // Arrange
        Long id = 1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }


    @Test
    @Tag("get")
    void ServiceUtils_get_UserIdInvalid_Negative() {
        // Arrange
        Long id = -1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get")
    void ServiceUtils_get_UserIdInvalid_Zero() {
        // Arrange
        Long id = 0L;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get")
    void ServiceUtils_get_UserIdInvalid_Null() {
        // Arrange
        Long id = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    private CreateUpdateUserDTO createValidDTO() {
        return CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_UserNotFound() {
        // Arrange
        Long id = 1L;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_UserIdInvalid_Negative() {
        // Arrange
        Long id = -1L;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_UserIdInvalid_Zero() {
        // Arrange
        Long id = 0L;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_UserIdInvalid_Null() {
        // Arrange
        Long id = null;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_Null() {
        // Arrange
        Long id = userService.create(CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build());
        CreateUpdateUserDTO dto = null;

        assertThatThrownBy(() -> userService.update(id, dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.DTO.NULL)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


//    @Test
//    @Tag("get")
//    @Disabled
//    void ServiceUtils_get_ThrowsUnhandledException() {
    // I couldn't implement this, but has the same handling for all methods;
    // in 'create' al could implement
//    }


    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdatePasswordInvalid() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().name(ERole.ROLE_USER).build())))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO updatePasswordDTO = CreateUpdateUserDTO.builder()
                .password("1234567")
                .build();

        // Act
        assertThatThrownBy(() -> userService.update(original.getId(), updatePasswordDTO))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_ThrowsUnhandledException_ThenGenericResponse() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().name(ERole.ROLE_USER).build())))
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
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Tag("delete")
    void ServiceUtils_delete_UserIdInvalid_Negative() {
        // Arrange
        Long id = -1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void ServiceUtils_delete_UserIdInvalid_Zero() {
        // Arrange
        Long id = 0L;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void ServiceUtils_delete_UserIdInvalid_Null() {
        // Arrange
        Long id = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void ServiceUtils_delete_UserNotFound() {
        // Arrange
        Long id = 1L;

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }
//
//    @Test
//    @Tag("delete")
//    @Disabled()
//    void ServiceUtils_delete_ThrowsUnhandledException() {
//        // I couldn't implement this
//  }

    @Test
    @Tag("get(pageable)")
        // todo: correct sintaxis "()"
    void ServiceUtils_get_PageableNull_ThenGenericResponse() {
        // Arrange
        Pageable pageable = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageStartingWith(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get(pageable)")
    void ServiceUtils_get_Pageable_SortByInvalid_NonexistentAttribute_thenGenericFail() {
        // Arrange
        int pageNum = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "ttt"));

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))

                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageStartingWith("No property") // No property 'ttt' found for type 'UserEntity'
                .hasMessageEndingWith("found")
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    @Test
    @Tag(" DataIntegrityViolationException")
    void AdviceUserControllerTest_createUser_DataIntegrityViolationException_Then409() throws Exception {

        // Arrange
        CreateUpdateUserDTO dto = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build();
        userService.create(dto);

        dto = CreateUpdateUserDTO.builder()
                .username("cris6h16") // same username
                .password("12345678")
                .email("cris6h16@ingithub.com")
                .build();

        // Act & Assert
        CreateUpdateUserDTO finalDto = dto;
        assertThatThrownBy(() -> userService.create(finalDto))
                .isInstanceOf(UserServiceTransversalException.class)
//                .hasMessage(Cons.User.Constrains.USERNAME_UNIQUE_MSG) // the passed message is based on exception.message.contains("<unique constrain name>"), but with H2 (testing) in its exception doesn't pass the constraint name of the violated
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.CONFLICT);
    }


}


//    @Test
//    @Tag("get(pageable)")
//    @Disabled
//    void ServiceUtils_get_Pageable_UnhandledException() {
// I couldn't implement this, but has the same handling for all methods;
// in 'create' al could implement


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
