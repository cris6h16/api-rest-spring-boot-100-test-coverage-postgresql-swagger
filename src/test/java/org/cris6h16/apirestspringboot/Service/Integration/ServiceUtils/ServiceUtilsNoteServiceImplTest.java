package org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
//@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED) // todo: docs my trouble with a transactional here which make fail to the transaction on @Service
public class ServiceUtilsNoteServiceImplTest {
    @Autowired
    private NoteService noteService;
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

    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_idInvalid_Negative() {
        // Arrange
        Long userId = -1L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_idInvalid_Zero() {
        // Arrange
        Long userId = 0L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_idInvalid_Null() {
        // Arrange
        Long userId = null;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("create")
    void ServiceUtilsNoteServiceImplTest_create_User_NotFound() {
        // Arrange
        Long userId = 1L;
        CreateNoteDTO note = new CreateNoteDTO("title", "content");

        // Act && Assert
        assertThatThrownBy(() -> noteService.create(note, userId))
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
    }

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
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.Note.Validations.TITLE_IS_BLANK_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

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
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.Note.Validations.TITLE_IS_BLANK_MSG)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

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
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.Note.DTO.NULL)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

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
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessageContaining(Cons.Response.ForClient.GENERIC_ERROR)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Tag("get")
    @ParameterizedTest// todo: refactor others testing to parameterized to avoid boilerplate
    @ValueSource(longs = {0, -1})
    void ServiceUtilsNoteServiceImplTest_get_idInvalidUserId(Long userId) {
        // Arrange
        Long noteId = 1L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.get(noteId, userId))
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessage(Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Tag("get")
    void ServiceUtilsNoteServiceImplTest_get_UserNotFound() {
        // Arrange
        Long userId = 1L; // checked first
        Long noteId = 1L;

        // Act && Assert
        assertThatThrownBy(() -> noteService.get(noteId, userId))
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
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
                .isInstanceOf(AbstractExceptionWithStatus.class)
                .hasMessage(Cons.Note.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("recommendedStatus", HttpStatus.NOT_FOUND);
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
