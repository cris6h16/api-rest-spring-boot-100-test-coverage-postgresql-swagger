package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


//@SpringBootTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
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
        NoteEntity note = createNoteEntities().iterator().next();

        when(userService.validateIdAndGetUser(any(Long.class)))
                .thenReturn(user);
        when(noteService.validateIdAndGetNote(any(Long.class), any(UserEntity.class))
                .thenReturn(Optional.of(note)));


        user.setNotes(createNoteEntities());
        userRepository.saveAndFlush(user);
        NoteEntity firstNoteEntity = user.getNotes().iterator().next();

        // Act
        PublicNoteDTO publicNote = noteService.get(firstNoteEntity.getId(), user.getId());

        // Assert
        assertThat(publicNote)
                .hasFieldOrPropertyWithValue("id", firstNoteEntity.getId())
                .hasFieldOrPropertyWithValue("title", firstNoteEntity.getTitle())
                .hasFieldOrPropertyWithValue("content", firstNoteEntity.getContent())
                .hasFieldOrPropertyWithValue("updatedAt", firstNoteEntity.getUpdatedAt());
    }


    @Test
    @Tag("put")
    void NoteServiceImplTest_put_ValidDTO_Replaces_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithId();
        user.setNotes(createNoteEntities());
        userRepository.saveAndFlush(user);

        NoteEntity firstNoteEntity = user.getNotes().iterator().next();
        CreateNoteDTO putDTO = new CreateNoteDTO("new title", "new content");

        // Act
        noteService.put(firstNoteEntity.getId(), putDTO, user.getId());

        // Assert
        NoteEntity updatedNote = noteRepository.findByIdAndUser(firstNoteEntity.getId(), user).orElse(null);
        assertThat(updatedNote)
                .hasFieldOrPropertyWithValue("title", putDTO.getTitle())
                .hasFieldOrPropertyWithValue("content", putDTO.getContent());
    }


    @Test
    @Tag("delete")
    void NoteServiceImplTest_delete_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithId();
        user.setNotes(createNoteEntities());
        userRepository.saveAndFlush(user);

        int notesSize = noteRepository.findByUser(user).size();
        NoteEntity firstNoteEntity = user.getNotes().iterator().next();

        // Act
        noteService.delete(firstNoteEntity.getId(), user.getId());

        // Assert
        int sizeNow = noteRepository.findByUser(user).size();
        assertThat(sizeNow).isEqualTo(notesSize - 1);
        assertThat(noteRepository.findById(firstNoteEntity.getId())).isEmpty();
    }

    @Test
    @Tag("get(pageable)")
    void NoteServiceImplTest_getPageable_Successful() {
        // Arrange
        UserEntity user = userRepository.saveAndFlush(createUserEntityWithId());
        List<NoteEntity> notes = noteRepository.saveAllAndFlush(createNoteEntities(10, user));

        Pageable pageable = PageRequest.of(
                0,
                17,
                Sort.by(Sort.Direction.ASC, "id")
        );
        // Act
        List<PublicNoteDTO> publicNotes = noteService.getPage(pageable, user.getId());

        // Assert
        assertThat(publicNotes).hasSize(notes.size());

    }

    Set<NoteEntity> createNoteEntities(int size, UserEntity user) {
        Set<NoteEntity> notes = new HashSet<>();
        for (int i = 0; i < size; i++) {
            notes.add(NoteEntity.builder()
                    .title("title" + i)
                    .content("content" + i)
                    .user(user)
                    .build());
        }
        return notes;
    }


    UserEntity createUserEntityWithId() {
        return UserEntity.builder()
                .id(1L)
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
    NoteEntity createNoteEntityWithId(UserEntity user) {
        return NoteEntity.builder()
                .id(1L)
                .title("title1")
                .content("content1")
                .user(user)
                .build();
    }

}
