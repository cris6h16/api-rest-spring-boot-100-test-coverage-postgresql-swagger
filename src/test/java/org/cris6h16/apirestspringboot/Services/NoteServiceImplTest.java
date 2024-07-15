package org.cris6h16.apirestspringboot.Services;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.AnyNoteDTOIsNullException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.NoteNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.TitleIsBlankException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Repositories.NoteRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class NoteServiceImplTest {

    @Mock
    NoteRepository noteRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserServiceImpl userService;

    @InjectMocks
    NoteServiceImpl noteService;

    @BeforeEach
    void setUp() {
        Mockito.reset(noteRepository, userRepository, userService);
    }

    @Test
    @Tag("create")
    void create_Successful() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        UserEntity uDB = mock(UserEntity.class);
        NoteEntity nDB = mock(NoteEntity.class);
        CreateNoteDTO toCreate = mock(CreateNoteDTO.class);

        when(toCreate.getTitle()).thenReturn("cris6h16's note");
        when(toCreate.getContent()).thenReturn("note content");
        when(nDB.getId()).thenReturn(noteId);

        when(userRepository.findById(any())).thenReturn(Optional.of(uDB));
        when(noteRepository.saveAndFlush(any())).thenReturn(nDB);

        // Act
        Long savedNoteId = noteService.create(toCreate, userId);

        // Assert
        assertThat(savedNoteId)
                .isNotNull()
                .isEqualTo(noteId);
        verify(userRepository).findById(userId);
        verify(noteRepository).saveAndFlush(argThat(passedToDB ->
                passedToDB.getTitle().equals(toCreate.getTitle()) &&
                        passedToDB.getContent().equals(toCreate.getContent()) &&
                        passedToDB.getUser().equals(uDB) &&
                        passedToDB.getUpdatedAt() != null &&
                        passedToDB.getUpdatedAt().getTime() <= System.currentTimeMillis()
        ));
    }


    @Test
    @Tag("create")
    void create_DTONull_ThenAnyNoteDTOIsNullException() {
        // Arrange
        Long userId = 1L;
        CreateNoteDTO toCreate = null;

        // Act & Assert
        assertThatThrownBy(() -> noteService.create(toCreate, userId))
                .isInstanceOf(AnyNoteDTOIsNullException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.Note.DTO.NULL)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Tag("create")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void create_userIdNullOrLessThanOne_ThenInvalidIdException(Long userId) {
        // Arrange
        userId = userId == -999 ? null : userId;
        CreateNoteDTO toCreate = mock(CreateNoteDTO.class);

        // Act & Assert
        final Long finalUserId = userId;
        assertThatThrownBy(() -> noteService.create(toCreate, finalUserId))
                .isInstanceOf(InvalidIdException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Tag("create")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty"})
    void create_titleNullOrBlankOrEmpty_ThenTitleIsBlankException(String title) {
        // Arrange
        Long userId = 1L;
        CreateNoteDTO toCreate = mock(CreateNoteDTO.class);
        title = switch (title) {
            case "null" -> null;
            case "blank" -> "   ";
            default -> "";
        };

        when(toCreate.getTitle()).thenReturn(title);

        // Act & Assert
        assertThatThrownBy(() -> noteService.create(toCreate, userId))
                .isInstanceOf(TitleIsBlankException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Tag("create")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty"})
    void create_contentNullOrBlankOrEmpty_ThenSuccessfulWithContentEmpty(String content) {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        UserEntity uDB = mock(UserEntity.class);
        NoteEntity nDB = mock(NoteEntity.class);
        content = switch (content) {
            case "null" -> null;
            case "blank" -> "   ";
            default -> "";
        };
        CreateNoteDTO toCreate = CreateNoteDTO.builder()
                .title("cris6h16's note")
                .content(content)
                .build();

        when(nDB.getId()).thenReturn(noteId);

        when(userRepository.findById(any())).thenReturn(Optional.of(uDB));
        when(noteRepository.saveAndFlush(any())).thenReturn(nDB);

        // Act
        Long savedNoteId = noteService.create(toCreate, userId);

        // Assert
        assertThat(savedNoteId)
                .isNotNull()
                .isEqualTo(noteId);
        verify(userRepository).findById(userId);
        verify(noteRepository).saveAndFlush(argThat(passedToDB ->
                passedToDB.getTitle().equals(toCreate.getTitle()) &&
                        passedToDB.getContent().equals(toCreate.getContent())
        ));
    }

    @Test
    @Tag("create")
    void create_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        CreateNoteDTO toCreate = mock(CreateNoteDTO.class);

        when(toCreate.getTitle()).thenReturn("title");
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> noteService.create(toCreate, userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository).findById(userId);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @Tag("getByIdAndUserId")
    void getByIdAndUserId_Successful() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        NoteEntity nDB = NoteEntity.builder()
                .id(noteId)
                .title("cris6h16's note")
                .content("note content")
                .updatedAt(new Date())
                .build();

        when(noteRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(nDB));

        // Act
        PublicNoteDTO dto = noteService.getByIdAndUserId(noteId, userId);

        // Assert
        assertThat(dto).isNotNull();
        verify(noteRepository).findByIdAndUserId(noteId, userId);
    }

    @Tag("getByIdAndUserId")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void getByIdAndUserId_userIdOrNoteId_NullOrLessThanOne_ThenInvalidIdException(Long invalidId) {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        invalidId = (invalidId == -999) ? null : invalidId;

        // Act & Assert
        for (int i = 0; i < 2; i++) {
            final Long finalUserId = (i == 0) ? invalidId : userId;
            final Long finalNoteId = (i == 1) ? invalidId : noteId;
            assertThatThrownBy(() -> noteService.getByIdAndUserId(finalNoteId, finalUserId))
                    .isInstanceOf(InvalidIdException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        }
        verify(userRepository, never()).findById(any());
        verify(noteRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    @Tag("getByIdAndUserId")
    void getByIdAndUserId_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;

        when(noteRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> noteService.getByIdAndUserId(noteId, userId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.Note.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(noteRepository).findByIdAndUserId(noteId, userId);
    }


    @Test
    @Tag("putByIdAndUserId")
    void putByIdAndUserId_ByIdAndUserId_NoteNotFound_ThenCreateSuccessful() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        UserEntity uDB = mock(UserEntity.class);
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("cris6h16's note")
                .content("github.com/cris6h16")
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(uDB));
        when(noteRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        // Act
        noteService.putByIdAndUserId(noteId, userId, dto);

        // Assert
        verify(userRepository).findById(userId);
        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository).saveAndFlush(argThat(passedToDB ->
                passedToDB.getId().equals(noteId) &&
                        passedToDB.getTitle().equals(dto.getTitle()) &&
                        passedToDB.getContent().equals(dto.getContent()) &&
                        passedToDB.getUser().equals(uDB) &&
                        passedToDB.getUpdatedAt() != null &&
                        passedToDB.getUpdatedAt().getTime() <= System.currentTimeMillis()
        ));
    }

    @Test
    @Tag("putByIdAndUserId")
    void putByIdAndUserId_ByIdAndUserId_NoteFound_ThenUpdateSuccessful() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        UserEntity uDB = mock(UserEntity.class);
        NoteEntity nDB = NoteEntity.builder()
                .id(noteId)
                .title("title")
                .content("content")
                .updatedAt(new Date())
                .user(uDB)
                .build();
        CreateNoteDTO toPutDto = CreateNoteDTO.builder()
                .title("cris6h16's note")
                .content("github.com/cris6h16")
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(uDB));
        when(noteRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(nDB));

        // Act
        noteService.putByIdAndUserId(noteId, userId, toPutDto);

        // Assert
        verify(userRepository).findById(userId);
        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository).saveAndFlush(argThat(passedToDB ->
                        passedToDB.getId().equals(noteId) &&
                        passedToDB.getTitle().equals(toPutDto.getTitle()) &&
                        passedToDB.getContent().equals(toPutDto.getContent()) &&
                        passedToDB.getUser().equals(uDB) &&
                        passedToDB.getUpdatedAt() != null &&
                        passedToDB.getUpdatedAt().getTime() <= System.currentTimeMillis()
        ));
    }

    @Tag("putByIdAndUserId")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void putByIdAndUserId_ByIdAndUserId_userIdOrNoteIdNullOrLessThanOne_ThenInvalidIdException(Long invalidId) {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        invalidId = (invalidId == -999) ? null : invalidId;
        CreateNoteDTO dto = mock(CreateNoteDTO.class);

        // Act & Assert
        for (int i = 0; i < 2; i++) {
            final Long finalUserId = (i == 0) ? invalidId : userId;
            final Long finalNoteId = (i == 1) ? invalidId : noteId;
            assertThatThrownBy(() -> noteService.putByIdAndUserId(finalNoteId, finalUserId, dto))
                    .isInstanceOf(InvalidIdException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        }
        verify(userRepository, never()).findById(any());
        verify(noteRepository, never()).findByIdAndUserId(any(), any());
        verify(noteRepository, never()).saveAndFlush(any());
    }

    @Test
    @Tag("putByIdAndUserId")
    void putByIdAndUserId_ByIdAndUserId_DTONull_ThenAnyNoteDTOIsNullException() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;

        // Act & Assert
        assertThatThrownBy(() -> noteService.putByIdAndUserId(noteId, userId, null))
                .isInstanceOf(AnyNoteDTOIsNullException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.Note.DTO.NULL)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).findById(any());
        verify(noteRepository, never()).findByIdAndUserId(any(), any());
        verify(noteRepository, never()).saveAndFlush(any());
    }

    @Tag("putByIdAndUserId")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty"})
    void putByIdAndUserId_ByIdAndUserId_titleNullOrBlankOrEmpty_ThenTitleIsBlankException(String title) {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        title = switch (title) {
            case "null" -> null;
            case "blank" -> "   ";
            default -> "";
        };
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title(title)
                .content("github.com/cris6h16")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> noteService.putByIdAndUserId(noteId, userId, dto))
                .isInstanceOf(TitleIsBlankException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).findById(any());
        verify(noteRepository, never()).findByIdAndUserId(any(), any());
        verify(noteRepository, never()).saveAndFlush(any());
    }

    @Tag("putByIdAndUserId")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty"})
    void putByIdAndUserId_ByIdAndUserId_contentNullOrBlank_ThenSuccessfulWithContentEmpty(String content) {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        content = switch (content) {
            case "null" -> null;
            case "blank" -> "   ";
            default -> "";
        };
        UserEntity uDB = mock(UserEntity.class);
        NoteEntity nDB = NoteEntity.builder()
                .id(noteId)
                .title("title")
                .content("content")
                .updatedAt(new Date())
                .user(uDB)
                .build();
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("cris6h16's note")
                .content(content)
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(uDB));
        when(noteRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(nDB));
        when(noteRepository.saveAndFlush(any())).thenReturn(nDB);

        // Act
        noteService.putByIdAndUserId(noteId, userId, dto);

        // Assert
        verify(userRepository).findById(userId);
        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository).saveAndFlush(argThat(passedToDB -> {
                    return passedToDB.getTitle().equals(dto.getTitle()) &&
                            passedToDB.getContent().equals(dto.getContent()) &&
                            passedToDB.getUser().equals(uDB) &&
                            passedToDB.getUpdatedAt() != null &&
                            passedToDB.getUpdatedAt().getTime() <= System.currentTimeMillis();
                }
        ));
    }

    @Test
    @Tag("putByIdAndUserId")
    void putByIdAndUserId_ByIdAndUserId_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("cris6h16's note")
                .content("github.com/cris6h16")
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> noteService.putByIdAndUserId(noteId, userId, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository).findById(userId);
        verify(noteRepository, never()).findByIdAndUserId(any(), any());
        verify(noteRepository, never()).saveAndFlush(any());
    }


    @Test
    @Tag("deleteByIdAndUserId")
    void deleteByIdAndUserId_Successful() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(noteRepository.existsByIdAndUserId(noteId, userId)).thenReturn(true);

        // Act
        noteService.deleteByIdAndUserId(noteId, userId);

        // Assert
        verify(userRepository).existsById(userId);
        verify(noteRepository).existsByIdAndUserId(noteId, userId);
        verify(noteRepository).deleteByIdAndUserId(noteId, userId);
    }

    @Tag("deleteByIdAndUserId")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void deleteByIdAndUserId_userIdOrNoteId_NullOrLessThanOne_ThenInvalidIdException(Long invalidId) {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;
        invalidId = (invalidId == -999) ? null : invalidId;

        // Act & Assert
        for (int i = 0; i < 2; i++) {
            final Long finalUserId = (i == 0) ? invalidId : userId;
            final Long finalNoteId = (i == 1) ? invalidId : noteId;
            assertThatThrownBy(() -> noteService.deleteByIdAndUserId(finalNoteId, finalUserId))
                    .isInstanceOf(InvalidIdException.class)
                    .hasFieldOrPropertyWithValue("reason", Cons.CommonInEntity.ID_INVALID)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        }
        verify(userRepository, never()).existsById(any());
        verify(noteRepository, never()).existsByIdAndUserId(any(), any());
        verify(noteRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    @Tag("deleteByIdAndUserId")
    void deleteByIdAndUserId_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;

        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> noteService.deleteByIdAndUserId(noteId, userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository).existsById(userId);
        verify(noteRepository, never()).existsByIdAndUserId(any(), any());
        verify(noteRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    @Tag("deleteByIdAndUserId")
    void deleteByIdAndUserId_NoteNotFound_ThenNoteNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long noteId = 11L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(noteRepository.existsByIdAndUserId(noteId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> noteService.deleteByIdAndUserId(noteId, userId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.Note.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository).existsById(userId);
        verify(noteRepository).existsByIdAndUserId(noteId, userId);
        verify(noteRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    @Tag("getPage")
    void getPage_Successful() {
        // Arrange
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id"));
        List<NoteEntity> entities = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            entities.add(NoteEntity.builder()
                    .id((long) i)
                    .title("title" + i)
                    .content("content" + i)
                    .updatedAt(new Date())
                    .build());
        }

        when(userRepository.existsById(userId)).thenReturn(true);
        when(noteRepository.findByUserId(userId, pageable)).thenReturn(new PageImpl<>(entities));

        // Act
        List<PublicNoteDTO> dtos = noteService.getPage(pageable, userId);

        // Assert
        verify(userRepository).existsById(userId);
        verify(noteRepository).findByUserId(userId, pageable);
        assertThat(dtos)
                .isNotNull()
                .isNotEmpty()
                .hasSize(entities.size());
        for (int i = 0; i < entities.size(); i++) {
            assertThat(dtos.get(i))
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("id", entities.get(i).getId())
                    .hasFieldOrPropertyWithValue("title", entities.get(i).getTitle())
                    .hasFieldOrPropertyWithValue("content", entities.get(i).getContent())
                    .hasFieldOrPropertyWithValue("updatedAt", entities.get(i).getUpdatedAt());
        }
    }


    @Test
    @Tag("getPage")
    void getPage_PageableNull_ThenIllegalArgumentException() {
        // Arrange
        Long userId = 1L;
        PageRequest pageable = null;

        // Act & Assert
        assertThatThrownBy(() -> noteService.getPage(pageable, userId))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).existsById(any());
        verify(noteRepository, never()).findByUserId(any(), any());
    }

    @Tag("getPage")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void getPage_userIdNullOrLessThanOne_ThenInvalidIdException(Long userId) {
        // Arrange
        userId = (userId == -999) ? null : userId;
        PageRequest pageable = mock(PageRequest.class);

        // Act & Assert
        final Long finalUserId = userId;
        assertThatThrownBy(() -> noteService.getPage(pageable, finalUserId))
                .isInstanceOf(InvalidIdException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).existsById(any());
        verify(noteRepository, never()).findByUserId(any(), any());
    }

    @Test
    @Tag("getPage")
    void getPage_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        PageRequest pageable = mock(PageRequest.class);

        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> noteService.getPage(pageable, userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository).existsById(userId);
        verify(noteRepository, never()).findByUserId(any(), any());
    }

    @Test
    @Tag("getPage")
    void getPage_EmptyPage_ThenEmptyList() {
        // Arrange
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("id"));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(noteRepository.findByUserId(userId, pageable)).thenReturn(new PageImpl<>(new ArrayList<>()));

        // Act
        List<PublicNoteDTO> dtos = noteService.getPage(pageable, userId);

        // Assert
        assertThat(dtos)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @Tag("deleteAll")
    void deleteAll_Successful() {
        doNothing().when(noteRepository).deleteAll();
        noteService.deleteAll();
        verify(noteRepository).deleteAll();
    }

}
