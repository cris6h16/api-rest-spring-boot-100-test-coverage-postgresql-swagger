package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repositories.NoteRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.cris6h16.apirestspringboot.Services.NoteServiceImpl;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Controller.Path.NOTE_PATH;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for {@link NoteController}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
class NoteControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    UserEntity userEntity;
    String noEncryptedPassword = "12345678";
    @Autowired
    private NoteServiceImpl noteServiceImpl;


    @BeforeEach
    void setUp() {
        userService.deleteAll();  // N + 1
        noteServiceImpl.deleteAll();

        Long id = userService.create(CreateUserDTO.builder()
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password(noEncryptedPassword)
                .build());
        assertThat(userRepository.existsById(id)).isTrue();
        userEntity = userRepository.findById(id).get();
    }


    // -------------------------------- CREATE --------------------------------\\

    @Test
    void create_successful_Then201_Created() {
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("My First Note")
                .content("note of cris6h16")
                .build();

        ResponseEntity<Void> response = this.restTemplate
                .withBasicAuth(userEntity.getUsername(), noEncryptedPassword)
                .postForEntity(NOTE_PATH, dto, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        String location = response.getHeaders().getLocation().toString();
        assertThat(location).matches(NOTE_PATH + "/\\d+"); // d = digit ( 0 - 9 ), + = one or more
        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        NoteEntity noteEntity = noteRepository.findById(id).orElse(null);
        assertThat(noteEntity)
                .isNotNull()
                .hasFieldOrPropertyWithValue("title", dto.getTitle())
                .hasFieldOrPropertyWithValue("content", dto.getContent());
        assertThat(noteEntity.getUpdatedAt()).isBeforeOrEqualTo(new Date());
    }
    // -------------------------------- GET PAGE --------------------------------\\


    @Test
    void getPage_successful_Then200_Ok() {
        List<CreateNoteDTO> createNoteDTOList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            createNoteDTOList.add(CreateNoteDTO.builder()
                    .title("My First Note " + i)
                    .content("note of cris6h16 " + i)
                    .build());
        }

        createNoteDTOList.forEach(dto -> {
            Long id = noteServiceImpl.create(dto, userEntity.getId());
        });


        URI uri = UriComponentsBuilder.fromPath(NOTE_PATH)
                .queryParam("page", 0)
                .queryParam("size", 25)
                .queryParam("sort", "id,desc")
                .build().toUri();

        ResponseEntity<PublicNoteDTO[]> response = this.restTemplate
                .withBasicAuth(userEntity.getUsername(), noEncryptedPassword)
                .getForEntity(uri, PublicNoteDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(24);

        List<PublicNoteDTO> publicNoteDTOList = Arrays.stream(response.getBody()).toList();
        assertThat(publicNoteDTOList).isSortedAccordingTo(Comparator.comparing(PublicNoteDTO::getId).reversed());

        for (int i = 0; i < createNoteDTOList.size(); i++) {
            CreateNoteDTO createDto = createNoteDTOList.get(i);
            PublicNoteDTO publicDto = publicNoteDTOList.get(createNoteDTOList.size() - i - 1); // reversed

            assertThat(createDto.getTitle()).isEqualTo(publicDto.getTitle());
            assertThat(createDto.getContent()).isEqualTo(publicDto.getContent());
        }

    }

    // -------------------------------- GET --------------------------------\\


    @Test
    void getByIdAndUserId_successful_Then200_Ok() {
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("My First Note")
                .content("note of cris6h16")
                .build();

        Long note_id = noteServiceImpl.create(dto, userEntity.getId());

        ResponseEntity<PublicNoteDTO> response = this.restTemplate
                .withBasicAuth(userEntity.getUsername(), noEncryptedPassword)
                .getForEntity(NOTE_PATH + "/" + note_id, PublicNoteDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasFieldOrPropertyWithValue("id", note_id)
                .hasFieldOrPropertyWithValue("title", dto.getTitle())
                .hasFieldOrPropertyWithValue("content", dto.getContent());
    }

    // -------------------------------- PUT --------------------------------\\

    @Test
    void put_ByIdAndUserId_successful_Then204_NoContent() {
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("My First Note")
                .content("note of cris6h16")
                .build();
        Long id = noteServiceImpl.create(dto, userEntity.getId());
        boolean wasCreated = noteRepository.existsById(id);

        CreateNoteDTO putDto = CreateNoteDTO.builder()
                .title("My First Note Updated")
                .content("note of cris6h16 Updated")
                .build();

        this.restTemplate
                .withBasicAuth(userEntity.getUsername(), noEncryptedPassword)
                .put(NOTE_PATH + "/" + id, putDto);

        NoteEntity noteEntity = noteRepository.findById(id).orElse(null);
        assertTrue(wasCreated);
        assertThat(noteEntity)
                .isNotNull()
                .hasFieldOrPropertyWithValue("title", putDto.getTitle())
                .hasFieldOrPropertyWithValue("content", putDto.getContent());
        assertThat(noteEntity.getUpdatedAt()).isBeforeOrEqualTo(new Date());
    }

    // -------------------------------- DELETE --------------------------------\\

    @Test
    void delete_successful_Then204_NoContent() {
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("My First Note")
                .content("note of cris6h16")
                .build();
        Long noteId = noteServiceImpl.create(dto, userEntity.getId());
        boolean wasCreated = noteRepository.existsById(noteId);

        this.restTemplate
                .withBasicAuth(userEntity.getUsername(), noEncryptedPassword)
                .delete(NOTE_PATH + "/" + noteId);

        assertThat(wasCreated).isTrue();
        assertThat(noteRepository.existsById(noteId)).isFalse();
    }

}