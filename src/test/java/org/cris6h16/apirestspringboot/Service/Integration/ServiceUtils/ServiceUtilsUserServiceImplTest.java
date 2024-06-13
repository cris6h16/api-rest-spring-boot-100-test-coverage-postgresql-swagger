package org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link UserServiceImpl} and {@link ServiceUtils} integration.
 * <br>
 * the mentioned Service will delegate an {@link UserServiceTransversalException}
 * to {@link ServiceUtils} when any exception occurs in any method on the service,
 * remember that all methods in the service are wrapped in a try-catch block,
 * and into the catch block, the service will delegate the creation of the
 * exception {@link UserServiceTransversalException} to {@link ServiceUtils},
 * {@link ServiceUtils} will create the exception with the message && status...
 * <br>
 * Here we will test the behavior of the fails in the service ( exceptions
 * raised into the service methods or any other layer below ) and make I sure that the exception
 * raised is handled properly.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @implNote I should test mocking the database exceptions, but when {@link ServiceUtils}
 * create an {@link UserServiceTransversalException} with message && status
 * depends on the exception type and its message,
 * For mock database exceptions I need to find the exception type && the exact message
 * of that specific failure through debugging, logs, souts, etc. I consider it
 * tedious also it can trigger fails due to the manual process like not pass the exact message
 * in the mocked exception which will cause a generic response.
 * due to that I decided don't mock the database exceptions... Just once before test
 * this class {@link ServiceUtilsUserServiceImplTest} I must test and pass
 * the test of the database layer, entity layer(constrains && validations);
 * once I have passed those tests I'll be sure that this tests won't fail
 * due to the database layer or entity layer.<br>
 * once the mentioned tests were green then I can test this class.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
class ServiceUtilsUserServiceImplTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserServiceImpl userService;

    /**
     * Clean the database before each test
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRepository.flush();
        roleRepository.flush();
    }

    /**
     * Test the creation of a user with password less than 8 characters<br>
     * The unique verification on the service layer, due to I cannot verify
     * It in the entity layer, because the password is passed encrypted which
     * makes it always a length greater than 8.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
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


    /**
     * test the exception raised in {@link UserServiceImpl#create(CreateUpdateUserDTO)}
     * when it's called with a {@link CreateUpdateUserDTO} null.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.User.DTO#NULL},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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


    /**
     * test the unexpected exception raised in {@link UserServiceImpl#create(CreateUpdateUserDTO)}
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.Response.ForClient#GENERIC_ERROR},{@code recommendedStatus}={@link HttpStatus#INTERNAL_SERVER_ERROR}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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

    /**
     * test the exception raised in {@link UserServiceImpl#get(Long)}
     * when it's called with a user id that doesn't exist in the database.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.User.Fails#NOT_FOUND},{@code recommendedStatus}={@link HttpStatus#NOT_FOUND}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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


    /**
     * test the exception raised in {@link UserServiceImpl#get(Long)}
     * when it's called with an invalid user id
     * (negative, zero, null).
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.CommonInEntity#ID_INVALID},
     * {@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -99}) // -99 == null
    @Tag("get")
    void ServiceUtils_get_UserIdInvalid_Negative(Long id) {
        // Arrange
        id = (id == -99) ? null : id;

        // Act & Assert
        Long finalId = id;
        assertThatThrownBy(() -> userService.get(finalId))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * create a valid {@link CreateUpdateUserDTO} <br>
     *
     * @return the created one
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private CreateUpdateUserDTO createValidDTO() {
        return CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@example.com")
                .build();
    }

    /**
     * test the exception raised in {@link UserServiceImpl#validateIdAndGetUser(Long)}
     * when {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)} is called
     * with a user id that doesn't exist in the database.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.User.Fails#NOT_FOUND},{@code recommendedStatus}={@link HttpStatus#NOT_FOUND}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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

    /**
     * test the exception raised in {@link ServiceUtils#validateId(Long)}
     * when {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)} is called
     * with an invalid user id (negative, zero, null).
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.CommonInEntity#ID_INVALID},
     * {@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -99}) // -99 == null
    @Tag("update")
    void ServiceUtils_update_UserIdInvalid(Long id) {
        // Arrange
        id = (id == -99) ? null : id;
        CreateUpdateUserDTO dto = createValidDTO();

        // Act & Assert
        Long finalId = id;
        assertThatThrownBy(() -> userService.update(finalId, dto))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * test the exception raised in {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)}
     * when it's called with a {@link CreateUpdateUserDTO} null.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.User.DTO#NULL},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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


    /**
     * test the exception raised in {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)}
     * when it's called with a {@link CreateUpdateUserDTO#getPassword()} less than 8 characters.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.User.Validations.InService#PASS_IS_TOO_SHORT_MSG},
     * {@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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

        // Act & Assert
        assertThatThrownBy(() -> userService.update(original.getId(), updatePasswordDTO))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * test an unexpected exception raised in {@link UserServiceImpl#update(Long, CreateUpdateUserDTO)}
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.Response.ForClient#GENERIC_ERROR},
     * {@code recommendedStatus}={@link HttpStatus#INTERNAL_SERVER_ERROR}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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
                throw new ArrayIndexOutOfBoundsException(Cons.TESTING.UNHANDLED_ERROR_WITH_TESTING_PURPOSES); // random exception
            }
        };

        // Act & Assert
        assertThatThrownBy(() -> userService.update(original.getId(), thrower))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * test the exception raised in {@link ServiceUtils#validateId(Long)}
     * when {@link UserServiceImpl#delete(Long)} is called with an invalid user id
     * (negative, zero, null).
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.CommonInEntity#ID_INVALID},
     * {@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -99}) // -99 == null
    @Tag("delete")
    void ServiceUtils_delete_UserIdInvalid_Negative(Long id) {
        // Arrange
        id = (id == -99) ? null : id;

        // Act & Assert
        Long finalId = id;
        assertThatThrownBy(() -> userService.delete(finalId))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * test the exception raised in {@link UserServiceImpl#validateIdAndGetUser(Long)}
     * when {@link UserServiceImpl#delete(Long)} is called with a user id that doesn't exist in the database.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.User.Fails#NOT_FOUND},
     * {@code recommendedStatus}={@link HttpStatus#NOT_FOUND}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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

    /**
     * test an unexpected exception raised in {@link UserServiceImpl#get(Pageable)}
     * when it's called with a {@link Pageable} null.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.Response.ForClient#GENERIC_ERROR},
     * {@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("getPage")
    void ServiceUtils_get_PageableNull_ThenGenericResponse() {
        // Arrange
        Pageable pageable = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.get(pageable))
                .isInstanceOf(UserServiceTransversalException.class)
                .hasMessageStartingWith(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * test an exception raised in {@link UserRepository#findAll(Pageable)}
     * when it's called with a {@link PageRequest} containing a non-existent attribute
     * to sort the results.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [{@code message}="No property '<>' found",
     * {@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("getPage")
    void ServiceUtils_get_Pageable_SortByInvalid_NonexistentAttribute_then400() {
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


    /**
     * test an exception raised in {@link UserRepository#saveAndFlush(Object)}
     * when {@link UserServiceImpl#create(CreateUpdateUserDTO)} is called violating
     * the unique constraint of the username.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link UserServiceTransversalException}
     * <br>
     * [<s>{@code message}={@link Cons.User.Constrains#USERNAME_UNIQUE_MSG}</s>,
     * {@code recommendedStatus}={@link HttpStatus#CONFLICT}]
     *
     * @implNote the response message isn't verified due to It'll have a different message
     * in H2(used for test) and PostgreSQL (used in production).
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("DataIntegrityViolationException")
    void AdviceUserControllerTest_createUser_DataIntegrityViolationException_Then409() throws Exception {

        // Arrange
        CreateUpdateUserDTO dto = createValidDTO();
        userService.create(dto);
        dto = CreateUpdateUserDTO.builder()
                .username(dto.getUsername()) // same username
                .password("cris6h16" + dto.getPassword())
                .email("cris6h16" + dto.getEmail())
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
//    @Tag("getPage")
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
