//package org.cris6h16.apirestspringboot.Service;
//
//import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
//import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
//import org.cris6h16.apirestspringboot.Entities.NoteEntity;
//import org.cris6h16.apirestspringboot.Entities.UserEntity;
//import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
//import org.cris6h16.apirestspringboot.Repository.NoteRepository;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.Mockito.*;
//
///**
// * Test class for {@link NoteServiceImpl}, here I just test when
// * the test is successful, due to all methods in the mentioned
// * service are wrapped in a try-catch block, in the catch
// * we delegate a creation of {@link NoteServiceTransversalException}
// * to {@link ServiceUtils}, then any exception threw in the methods of
// * {@link NoteServiceImpl} should be tested as integration with {@link ServiceUtils}.
// *
// * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
// * @implNote {@link NoteServiceImpl} are tested in isolation, mocking the dependencies.
// * @since 1.0
// */
//@ExtendWith(MockitoExtension.class)
//public class NoteServiceImplTest {
//
//    @Mock
//    NoteRepository noteRepository;
//    @Mock
//    UserServiceImpl userService;
//
//    @InjectMocks
//    NoteServiceImpl noteService;
//
//    /**
//     * Test for {@link NoteServiceImpl#create(CreateNoteDTO, Long)}  when is successful.
//     * <br>
//     * Test: Create a note passing in {@link CreateNoteDTO} a correct title, but a null content
//     * then the {@link NoteEntity} passed to DB for be saved should have an empty content, not a null.
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Tag("create")
//    void NoteServiceImplTest_create_DTO_ContentNull_Successful() {
//        // Arrange
//        UserEntity savedUMock = createUserEntityWithId();
//        NoteEntity savedNMock = createNoteEntityWithId(savedUMock);
//
//        when(userService.validateIdAndGetUser(any(Long.class)))
//                .thenReturn(savedUMock);
//        when(noteRepository.saveAndFlush(any()))
//                .thenReturn(savedNMock);
//
//        CreateNoteDTO toCreate = new CreateNoteDTO(savedNMock.getTitle(), null);
//
//        // Act
//        Long savedNoteId = noteService.create(toCreate, savedUMock.getId());
//
//        // Assert
//        assertThat(savedNoteId).isNotNull();
//        assertThat(savedNMock.getId()).isEqualTo(savedNoteId);
//
//        verify(userService).validateIdAndGetUser(any(Long.class));
//        verify(noteRepository).saveAndFlush(argThat(noteEntity ->
//                noteEntity.getTitle().equals(toCreate.getTitle()) &&
//                        noteEntity.getContent().equals("")
//        ));
//    }
//
//    /**
//     * Test for {@link NoteServiceImpl#create(CreateNoteDTO, Long)}  when is successful.
//     * <br>
//     * Test: Create a note passing in {@link CreateNoteDTO} a correct title, but a blank content
//     * then the {@link NoteEntity} passed to DB for be saved should have a blank content, not a null.
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Tag("create")
//    void NoteServiceImplTest_create_DTO_ContentBlank_Successful() {
//        // Arrange
//        UserEntity savedUMock = createUserEntityWithId();
//        NoteEntity savedNMock = createNoteEntityWithId(savedUMock);
//        savedNMock.setContent("");
//
//        when(userService.validateIdAndGetUser(any(Long.class)))
//                .thenReturn(savedUMock);
//        when(noteRepository.saveAndFlush(any()))
//                .thenReturn(savedNMock);
//
//        CreateNoteDTO toCreate = new CreateNoteDTO(savedNMock.getTitle(), null);
//
//        // Act
//        Long savedNoteId = noteService.create(toCreate, savedUMock.getId());
//
//        // Assert
//        assertThat(savedNoteId).isNotNull();
//        assertThat(savedNMock.getId()).isEqualTo(savedNoteId);
//
//        verify(userService).validateIdAndGetUser(any(Long.class));
//        verify(noteRepository).saveAndFlush(argThat(noteEntity ->
//                noteEntity.getTitle().equals(toCreate.getTitle()) &&
//                        noteEntity.getContent().equals("")
//        ));
//    }
//
//
//    /**
//     * Test for {@link NoteServiceImpl#get(Long, Long)} when is successful.
//     * <br>
//     * Test: Create a note passing in {@link CreateNoteDTO} a correct title, but a null content
//     * then the {@link NoteEntity} passed to DB for be saved should have an empty content, not a null.
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Tag("get")
//    void NoteServiceImplTest_get_Successful() {
//        // Arrange
//        UserEntity user = createUserEntityWithId();
//        NoteEntity note = createNoteEntityWithId(user);
//
//        doNothing().when(serviceUtils).validateId(any(Long.class));
//        when(userService.validateIdAndGetUser(user.getId()))
//                .thenReturn(user);
//        when(noteRepository.findByIdAndUser(note.getId(), user))
//                .thenReturn(Optional.of(note));
//
//        // Act
//        PublicNoteDTO publicNote = noteService.get(note.getId(), user.getId());
//
//        // Assert
//        assertThat(publicNote)
//                .hasFieldOrPropertyWithValue("id", note.getId())
//                .hasFieldOrPropertyWithValue("title", note.getTitle())
//                .hasFieldOrPropertyWithValue("content", note.getContent())
//                .hasFieldOrPropertyWithValue("updatedAt", note.getUpdatedAt());
//    }
//
//
//    /**
//     * Test for {@link NoteServiceImpl#put(Long, CreateNoteDTO, Long)}  when is successful.
//     * <br>
//     * Test: Replace a note based on the id.
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Tag("put")
//    void NoteServiceImplTest_put_ValidDTO_Replaces_Successful() {
//        // Arrange
//        UserEntity user = createUserEntityWithId();
//        NoteEntity toUpdate = createNoteEntityWithId(user);
//        NoteEntity noteUpdated = createNoteEntityWithId(user);
//        noteUpdated.setTitle("new title");
//
//        when(userService.validateIdAndGetUser(any(Long.class)))
//                .thenReturn(user);
//        when(noteRepository.findByIdAndUser(any(Long.class), any(UserEntity.class))
//        ).thenReturn(Optional.of(noteUpdated));
//
//        CreateNoteDTO putDTO = new CreateNoteDTO("new title", "new content");
//
//        // Act
//        noteService.put(toUpdate.getId(), putDTO, user.getId());
//
//        // Assert
//        verify(userService).validateIdAndGetUser(any(Long.class));
//        verify(noteRepository).findByIdAndUser(any(Long.class), any(UserEntity.class));
//        verify(noteRepository).saveAndFlush(argThat(passedToDB ->
//                passedToDB.getTitle().equals(putDTO.getTitle()) &&
//                        passedToDB.getContent().equals(putDTO.getContent()) &&
//                        passedToDB.getId().equals(toUpdate.getId()) &&
//                        passedToDB.getUser().equals(user)
//        ));
//    }
//
//
//    /**
//     * Test for {@link NoteServiceImpl#delete(Long, Long)} when is successful.
//     * <br>
//     * Test: Delete a note based on the id.
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Tag("delete")
//    void NoteServiceImplTest_delete_Successful() {
//        // Arrange
//        UserEntity user = createUserEntityWithId();
//        NoteEntity note = createNoteEntityWithId(user);
//
//        doNothing().when(serviceUtils).validateId(any(Long.class));
//        when(userService.validateIdAndGetUser(any(Long.class)))
//                .thenReturn(user);
//        when(noteRepository.findByIdAndUser(note.getId(), user))
//                .thenReturn(Optional.of(note));
//        doNothing().when(noteRepository).delete(note);
//
//        // Act
//        noteService.delete(note.getId(), user.getId());
//
//        // Assert
//        verify(serviceUtils).validateId(any(Long.class));
//        verify(userService).validateIdAndGetUser(user.getId());
//        verify(noteRepository).findByIdAndUser(note.getId(), user);
//        verify(noteRepository).delete(note);
//    }
//
//
//    /**
//     * Test for {@link NoteServiceImpl#getPage(Pageable, Long)} when is successful.
//     * <br>
//     * Test: Get a page of notes based on the user id.
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Tag("get(pageable)")
//    void NoteServiceImplTest_getPageable_Successful() {
//        // Arrange
//        UserEntity user = createUserEntityWithId();
//        List<NoteEntity> list = createNoteEntities(user);
//        Pageable pageable = PageRequest.of(
//                0,
//                17,
//                Sort.by(Sort.Direction.ASC, "id")
//        );
//
//        when(userService.validateIdAndGetUser(user.getId()))
//                .thenReturn(user);
//        when(noteRepository.findByUser(user, pageable))
//                .thenReturn(new PageImpl<>(list));
//
//        // Act
//        List<PublicNoteDTO> publicNotes = noteService.getPage(pageable, user.getId());
//
//        // Assert
//        assertThat(publicNotes).hasSize(list.size());
//
//        for (int i = 0; i < list.size(); i++) {
//            assertThat(publicNotes.get(i))
//                    .hasFieldOrPropertyWithValue("id", list.get(i).getId())
//                    .hasFieldOrPropertyWithValue("title", list.get(i).getTitle())
//                    .hasFieldOrPropertyWithValue("content", list.get(i).getContent())
//                    .hasFieldOrPropertyWithValue("updatedAt", list.get(i).getUpdatedAt());
//        }
//
//    }
//
//
//    /**
//     * Create a {@link UserEntity} with id {@code 1}.
//     *
//     * @return the created one.
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    UserEntity createUserEntityWithId() {
//        return UserEntity.builder()
//                .id(1L)
//                .username("cris6h16")
//                .password("12345678")
//                .email("cris6h16@gmail.com")
//                .build();
//    }
//
//
//    /**
//     * Create a list of {@link NoteEntity} with 2 elements.
//     *
//     * @param user the user to set in the notes.
//     * @return the created list of notes.
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    List<NoteEntity> createNoteEntities(UserEntity user) {
//        List<NoteEntity> notes = new ArrayList<>(2);
//        notes.add(NoteEntity.builder()
//                .title("title1")
//                .content("content1")
//                .user(user)
//                .build());
//        notes.add(NoteEntity.builder()
//                .title("title2")
//                .content("content2")
//                .user(user)
//                .build());
//
//        return notes;
//    }
//
//    /**
//     * Create a {@link NoteEntity} with id {@code 1}.
//     *
//     * @param user the user to set in the note.
//     * @return the created one.
//     * @implNote The id is set to {@code 1}.
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    NoteEntity createNoteEntityWithId(UserEntity user) {
//        return NoteEntity.builder()
//                .id(1L)
//                .title("title1")
//                .content("content1")
//                .user(user)
//                .build();
//    }
//
//}
