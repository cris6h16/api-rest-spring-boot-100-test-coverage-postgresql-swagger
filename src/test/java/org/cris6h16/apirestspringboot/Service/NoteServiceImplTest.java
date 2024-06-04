package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


//@SpringBootTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency // todo: doc my trouble with enviroment variables
//@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED) // todo: docs my trouble with a transactional here which make fail to the transaction on @Service
@ExtendWith(MockitoExtension.class)
public class NoteServiceImplTest {
//    @Autowired
//    private NoteService noteService;
//    @Autowired
//    private NoteRepository noteRepository;
//    @Autowired
//    private UserRepository userRepository;

    @Mock
    NoteRepository noteRepository;
    @Mock
    UserServiceImpl userService;
    @Mock
    ServiceUtils serviceUtils;

    @InjectMocks
    NoteServiceImpl noteService;


    @Test
    @Tag("create")
    void NoteServiceImplTest_create_DTO_ContentNull_Successful() {
        // Arrange
        UserEntity savedUMock = createUserEntityWithId();
        NoteEntity savedNMock = createNoteEntityWithId(savedUMock);

        when(userService.validateIdAndGetUser(any(Long.class)))
                .thenReturn(savedUMock);
        when(noteRepository.saveAndFlush(any()))
                .thenReturn(savedNMock);

        CreateNoteDTO toCreate = new CreateNoteDTO(savedNMock.getTitle(), null);

        // Act
        Long savedNoteId = noteService.create(toCreate, savedUMock.getId());

        // Assert
        assertThat(savedNoteId).isNotNull();
        assertThat(savedNMock.getId()).isEqualTo(savedNoteId);

        verify(userService).validateIdAndGetUser(any(Long.class));
        verify(noteRepository).saveAndFlush(argThat(noteEntity ->
                noteEntity.getTitle().equals(toCreate.getTitle()) &&
                        noteEntity.getContent().equals("")
        ));
    }

    @Test
    @Tag("create")
    void NoteServiceImplTest_create_DTO_ContentBlank_Successful() {
        // Arrange
        UserEntity savedUMock = createUserEntityWithId();
        NoteEntity savedNMock = createNoteEntityWithId(savedUMock);
        savedNMock.setContent("");

        when(userService.validateIdAndGetUser(any(Long.class)))
                .thenReturn(savedUMock);
        when(noteRepository.saveAndFlush(any()))
                .thenReturn(savedNMock);

        CreateNoteDTO toCreate = new CreateNoteDTO(savedNMock.getTitle(), null);

        // Act
        Long savedNoteId = noteService.create(toCreate, savedUMock.getId());

        // Assert
        assertThat(savedNoteId).isNotNull();
        assertThat(savedNMock.getId()).isEqualTo(savedNoteId);

        verify(userService).validateIdAndGetUser(any(Long.class));
        verify(noteRepository).saveAndFlush(argThat(noteEntity ->
                noteEntity.getTitle().equals(toCreate.getTitle()) &&
                        noteEntity.getContent().equals("")
        ));
    }


    @Test
    @Tag("get")
    void NoteServiceImplTest_get_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithId();
        NoteEntity note = createNoteEntityWithId(user);

        doNothing().when(serviceUtils).validateId(any(Long.class));
        when(userService.validateIdAndGetUser(user.getId()))
                .thenReturn(user);
        when(noteRepository.findByIdAndUser(note.getId(), user))
                .thenReturn(Optional.of(note));

        // Act
        PublicNoteDTO publicNote = noteService.get(note.getId(), user.getId());

        // Assert
        assertThat(publicNote)
                .hasFieldOrPropertyWithValue("id", note.getId())
                .hasFieldOrPropertyWithValue("title", note.getTitle())
                .hasFieldOrPropertyWithValue("content", note.getContent())
                .hasFieldOrPropertyWithValue("updatedAt", note.getUpdatedAt());
    }


    @Test
    @Tag("put")
    void NoteServiceImplTest_put_ValidDTO_Replaces_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithId();
        NoteEntity toUpdate = createNoteEntityWithId(user);
        NoteEntity noteUpdated = createNoteEntityWithId(user);
        noteUpdated.setTitle("new title");

        when(userService.validateIdAndGetUser(any(Long.class)))
                .thenReturn(user);
        when(noteRepository.findByIdAndUser(any(Long.class), any(UserEntity.class))
        ).thenReturn(Optional.of(noteUpdated));

        CreateNoteDTO putDTO = new CreateNoteDTO("new title", "new content");

        // Act
        noteService.put(toUpdate.getId(), putDTO, user.getId());

        // Assert
        verify(userService).validateIdAndGetUser(any(Long.class));
        verify(noteRepository).findByIdAndUser(any(Long.class), any(UserEntity.class));
        verify(noteRepository).saveAndFlush(argThat(passedToDB ->
                passedToDB.getTitle().equals(putDTO.getTitle()) &&
                        passedToDB.getContent().equals(putDTO.getContent()) &&
                        passedToDB.getId().equals(toUpdate.getId()) &&
                        passedToDB.getUser().equals(user)
        ));
    }


    @Test
    @Tag("delete")
    void NoteServiceImplTest_delete_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithId();
        NoteEntity note = createNoteEntityWithId(user);

        doNothing().when(serviceUtils).validateId(any(Long.class));
        when(userService.validateIdAndGetUser(any(Long.class)))
                .thenReturn(user);
        when(noteRepository.findByIdAndUser(note.getId(), user))
                .thenReturn(Optional.of(note));
        doNothing().when(noteRepository).delete(note);

        // Act
        noteService.delete(note.getId(), user.getId());

        // Assert
        verify(serviceUtils).validateId(any(Long.class));
        verify(userService).validateIdAndGetUser(user.getId());
        verify(noteRepository).findByIdAndUser(note.getId(), user);
        verify(noteRepository).delete(note);
    }

    @Test
    @Tag("get(pageable)")
    void NoteServiceImplTest_getPageable_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithId();
        List<NoteEntity> list = createNoteEntities(user);
        Pageable pageable = PageRequest.of(
                0,
                17,
                Sort.by(Sort.Direction.ASC, "id")
        );

        when(userService.validateIdAndGetUser(user.getId()))
                .thenReturn(user);
        when(noteRepository.findByUser(user, pageable))
                .thenReturn(new PageImpl<>(list));

        // Act
        List<PublicNoteDTO> publicNotes = noteService.getPage(pageable, user.getId());

        // Assert
        assertThat(publicNotes).hasSize(list.size());

        for (int i = 0; i < list.size(); i++) {
            assertThat(publicNotes.get(i))
                    .hasFieldOrPropertyWithValue("id", list.get(i).getId())
                    .hasFieldOrPropertyWithValue("title", list.get(i).getTitle())
                    .hasFieldOrPropertyWithValue("content", list.get(i).getContent())
                    .hasFieldOrPropertyWithValue("updatedAt", list.get(i).getUpdatedAt());
        }

    }




    UserEntity createUserEntityWithId() {
        return UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }



    List<NoteEntity> createNoteEntities(UserEntity user) {
        List<NoteEntity> notes = new ArrayList<>(2);
        notes.add(NoteEntity.builder()
                .title("title1")
                .content("content1")
                .user(user)
                .build());
        notes.add(NoteEntity.builder()
                .title("title2")
                .content("content2")
                .user(user)
                .build());

        return notes;
    }

    NoteEntity createNoteEntityWithId(UserEntity user) {
        return NoteEntity.builder()
                .id(1L)
                .title("title1")
                .content("content1")
                .user(user)
                .build();
    }

}
