package org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link NoteServiceImpl} and {@link ServiceUtils} integration.
 * <br>
 * the mentioned Service will delegate an {@link NoteServiceTransversalException}
 * to {@link ServiceUtils} when any exception occurs in any method on the service,
 * remember that all methods in the service are wrapped in a try-catch block,
 * and into the catch block, the service will delegate the creation of the
 * exception {@link NoteServiceTransversalException} to {@link ServiceUtils},
 * {@link ServiceUtils} will create the exception with the message && status...
 * <br>
 * Here we will test the behavior of the fails in the service ( exceptions
 * raised into the service methods or any other layer below ) and make I sure that the exception
 * raised is handled properly.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @implNote I should test mocking the database exceptions, but when {@link ServiceUtils}
 * create an {@link NoteServiceTransversalException} with message && status
 * depends on the exception type and its message,
 * For mock database exceptions I need to find the exception type && the exact message
 * of that specific failure through debugging, logs, souts, etc. I consider it
 * tedious also it can trigger fails due the manual process,
 * then I decided don't mock the database exceptions... Just once before test
 * this class {@link ServiceUtilsNoteServiceImplTest} I must test and pass
 * the test of the database layer, entity layer(constrains && validations);
 * once I have passed those tests I'll be sure that this tests won't fail
 * due to the database layer or entity layer.<br>
 * once the mentioned tests were green then I can test this class.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
//@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED) // In each service method I'll use @Transactional, unable here to avoid conflicts
public class ServiceUtilsNoteServiceImplTest {
    @Autowired
    private NoteServiceImpl noteService;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
        userRepository.deleteAll();

        noteRepository.flush();
        userRepository.flush();
    }

    /**
     * Test the exception raised in the {@link ServiceUtils#validateId(Long)} when
     * {@link NoteService#create(CreateNoteDTO, Long)} is called with an invalid user id (negative).
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.CommonInEntity#ID_INVALID},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_idInvalid_Negative() {
        // Arrange
        Long userId = -1L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * Test the exception raised in the {@link ServiceUtils#validateId(Long)} when
     * {@link NoteService#create(CreateNoteDTO, Long)} is called with an invalid user id (zero).
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.CommonInEntity#ID_INVALID},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_idInvalid_Zero() {
        // Arrange
        Long userId = 0L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * Test the exception raised in the {@link ServiceUtils#validateId(Long)} when
     * {@link NoteService#create(CreateNoteDTO, Long)} is called with an invalid user id (null).
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.CommonInEntity#ID_INVALID},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_idInvalid_Null() {
        // Arrange
        Long userId = null;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * Test the exception raised in {@link UserServiceImpl#validateIdAndGetUser(Long)}
     * when {@link NoteService#create(CreateNoteDTO, Long)} is called with a user id that doesn't exist.
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.User.Fails#NOT_FOUND},{@code recommendedStatus}={@link HttpStatus#NOT_FOUND}]
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_User_NotFound() {
        // Arrange
        Long userId = 1L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

    /**
     * test the exception raised in {@link NoteRepository#saveAndFlush(Object)}
     * when {@link NoteService#create(CreateNoteDTO, Long)} is called
     * with a {@link CreateNoteDTO} with a null title.
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.Note.Validations#TITLE_IS_BLANK_MSG},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_DTO_Title_Null() {
        // --------- Arrange --------- \\
        // create entity & save
        UserEntity user = createUserEntity();
        userRepository.saveAndFlush(user);

        CreateNoteDTO note = new CreateNoteDTO(null, "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, user.getId()))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.Note.Validations.TITLE_IS_BLANK_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * test the exception raised in {@link NoteRepository#saveAndFlush(Object)}
     * when {@link NoteService#create(CreateNoteDTO, Long)} is called
     * with a {@link CreateNoteDTO} with a blank title.
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.Note.Validations#TITLE_IS_BLANK_MSG},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_DTO_Title_Blank() {
        // --------- Arrange --------- \\
        // create entity & save
        UserEntity user = createUserEntity();
        userRepository.saveAndFlush(user);

        CreateNoteDTO note = new CreateNoteDTO("       ", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, user.getId()))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.Note.Validations.TITLE_IS_BLANK_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * test the exception raised in {@link NoteService#create(CreateNoteDTO, Long)}
     * when it's called with a {@link CreateNoteDTO} null.
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.Note.DTO#NULL},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_DTO_Null() {
        // --------- Arrange --------- \\
        // create entity & save
        UserEntity user = createUserEntity();
        userRepository.saveAndFlush(user);
        CreateNoteDTO toCreate = null;

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(toCreate, user.getId()))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.Note.DTO.NULL)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     * test the exception raised in {@link NoteService#create(CreateNoteDTO, Long)}
     * when it throws an unexpected exception
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.Response.ForClient#GENERIC_ERROR},{@code recommendedStatus}={@link HttpStatus#INTERNAL_SERVER_ERROR}]
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_UnhandledException() {
        // --------- Arrange --------- \\
        // create entity & save
        UserEntity user = createUserEntity();
        userRepository.saveAndFlush(user);
        CreateNoteDTO toCreate = new CreateNoteDTO("title", "content") {
            @Override
            public String getTitle() {
                throw new NoSuchElementException("cris6h16's random exception");
            }
        };

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(toCreate, user.getId()))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Test the exception raised in the {@link ServiceUtils#validateId(Long)} when
     * {@link NoteService#get(Long, Long)} is called with an invalid user id (negative).
     *
     * <p>
     * then the exception threw should be:
     * <br>
     * {@link NoteServiceTransversalException}
     * <br>
     * [{@code message}={@link Cons.CommonInEntity#ID_INVALID},{@code recommendedStatus}={@link HttpStatus#BAD_REQUEST}]
     * </p>
     *
     * @param userId invalid user ids
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Tag("get")
    @ParameterizedTest// todo: refactor others testing to parameterized to avoid boilerplate
    @ValueSource(longs = {0, -1})
    void ServiceUtilsNoteServiceImplTest_get_idInvalidUserId(Long userId) {
        // Arrange
        Long noteId = 1L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.get(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    /**
     *
     */
    @Test
    @Tag("get")
    void ServiceUtilsNoteServiceImplTest_get_UserNotFound() {
        // Arrange
        Long userId = 1L; // checked first
        Long noteId = 1L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.get(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

    @Tag("get")
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void ServiceUtilsNoteServiceImplTest_get_idInvalidNoteId(Long noteId) {
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();

        // Act && Assert
        assertThatThrownBy(() -> noteService.get(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get")
    void ServiceUtilsNoteServiceImplTest_get_NoteNotFound() {
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();
        Long noteId = 1L; // checked 2nd

        // Act && Assert
        assertThatThrownBy(() -> noteService.get(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.Note.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

//
//    @Test
//    @Tag("get")
//    @Disabled
//    void ServiceUtilsNoteServiceImplTest_get_UnhandledException() {
//        // I couldn't find a way to throw an unhandled exception in the get method
//        // but with `ServiceUtilsNoteServiceImplTest_create_UnhandledException` is enough
//    }


    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @Tag("put")
    void ServiceUtilsNoteServiceImplTest_put_idInvalidUserId(Long userId) {
        // Arrange
        Long noteId = 1L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.put(noteId, note, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @Tag("put")
    void ServiceUtilsNoteServiceImplTest_put_idInvalidNoteId(Long noteId) {
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.put(noteId, note, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    @Test
    @Tag("put")
    void ServiceUtilsNoteServiceImplTest_put_UserNotFound() {
        // Arrange
        Long userId = 1L;
        Long noteId = 1L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.put(noteId, note, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "     ", "null"})
    @Tag("put")
    void ServiceUtilsNoteServiceImplTest_put_DTO_TitleInvalid(String title) {
        title = title.equals("null") ? null : title;
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();
        CreateNoteDTO putDTO = new CreateNoteDTO(title, "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.put(1L, putDTO, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.Note.Validations.TITLE_IS_BLANK_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("put")
    void ServiceUtilsNoteServiceImplTest_put_UnhandledException() {
        // Arrange
        UserEntity user = createUserEntity();
        user.setNotes(createNoteEntities());
        userRepository.saveAndFlush(user);

        NoteEntity firstNoteEntity = user.getNotes().iterator().next();
        CreateNoteDTO putDTO = new CreateNoteDTO("new title", "new content") {
            @Override
            public String getTitle() {
                throw new NoSuchElementException("cris6h16's random exception");
            }
        };

        // Act && Assert
        assertThatThrownBy(() -> noteService.put(firstNoteEntity.getId(), putDTO, user.getId()))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Tag("delete")
    void ServiceUtilsNoteServiceImplTest_delete_idInvalidUserId() {
        // Arrange
        Long userId = -1L;
        Long noteId = 1L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.delete(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void ServiceUtilsNoteServiceImplTest_delete_UserNotFound() {
        // Arrange
        Long userId = 1L;
        Long noteId = 1L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.delete(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

    @Test
    @Tag("delete")
    void ServiceUtilsNoteServiceImplTest_delete_idInvalidNoteId() {
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();
        Long noteId = 0L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.delete(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("delete")
    void ServiceUtilsNoteServiceImplTest_delete_NoteNotFound() {
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();
        Long noteId = 1L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.delete(noteId, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.Note.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }


    @Test
    @Tag("getPage")
    void ServiceUtilsNoteServiceImplTest_getPage_idInvalidUserId() {
        // Arrange
        Long userId = -1L;
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id"));

        // Act && Assert
        assertThatThrownBy(() -> noteService.getPage(pageRequest, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    @Test
    @Tag("getPage")
    void ServiceUtilsNoteServiceImplTest_getPage_UserNotFound() {
        // Arrange
        Long userId = 1L;
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id"));

        // Act && Assert
        assertThatThrownBy(() -> noteService.getPage(pageRequest, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

    @Test
    @Tag("getPage")
    void ServiceUtilsNoteServiceImplTest_getPage_nullPageRequest() {
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "cris6h16"));

        // Act && Assert
        assertThatThrownBy(() -> noteService.getPage(pageRequest, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessageStartingWith("No property ") // No property 'cris6h16' found
                .hasMessageEndingWith(" found")
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    @Test
    @Tag("getPage")
    void ServiceUtilsNoteServiceImplTest_getPage_NonexistentAttribute() {
        // Arrange
        Long userId = userRepository.saveAndFlush(createUserEntity()).getId();
        PageRequest pageRequest = null;

        // Act && Assert
        assertThatThrownBy(() -> noteService.getPage(pageRequest, userId))
                .isInstanceOf(NoteServiceTransversalException.class)
                .hasMessage(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }


    UserEntity createUserEntity() {
        return UserEntity.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }


    Set<NoteEntity> createNoteEntities() {
        Set<NoteEntity> notes = new HashSet<>();
        notes.add(NoteEntity.builder()
                .title("title1")
                .content("content1")
                .build());
        notes.add(NoteEntity.builder()
                .title("title2")
                .content("content2")
                .build());

        return notes;
    }
}
