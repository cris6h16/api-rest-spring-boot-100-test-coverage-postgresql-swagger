package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NoteControllerTest {

    @Autowired
    TestRestTemplate rt;
    @Autowired
    NoteRepository noteRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper; // deserialize JSON to Java objects

    String username = "cris6h16";
    String pass = "12345678";
    Long dbUserID;


    @BeforeEach
    void setUp() {
        String url = "/api/users";
        String email = "cristiamherrera21@gmail.com";

        CreateUserDTO user = new CreateUserDTO(username, pass, email);
        HttpEntity<CreateUserDTO> entity = new HttpEntity<>(user);
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, entity, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String[] location = res.getHeaders().getLocation().toString().split("/");
        dbUserID = Long.parseLong(location[location.length - 1]);
    }


    @Test
    @DirtiesContext
    void shouldCreateANote() {
        String url = "/api/notes";
        String title = "Hello I'm a title";
        String content = "Hello I'm its content";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<Void> res = rt
                .withBasicAuth(username, pass)
                .exchange(url, HttpMethod.POST, note, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //get id from res
        String[] parts = res.getHeaders().getLocation().toString().split("/");
        Long id = Long.parseLong(parts[parts.length - 1]);

        // Get the note
        Optional<NoteEntity> noteEntity = noteRepository.findById(id);
        assertThat(noteEntity.isPresent()).isTrue();

        // Check the note
        NoteEntity fromDB = noteEntity.get();
        assertThat(fromDB.getId()).isGreaterThan(0);
        assertThat(fromDB.getCreatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
        assertThat(fromDB.getUpdatedAt()).isNull();
        assertThat(fromDB.getDeletedAt()).isNull();
        assertThat(fromDB.getTitle()).isEqualTo(title);
        assertThat(fromDB.getContent()).isEqualTo(content);
    }

    @Test
    void shouldNotCreateANoteTitleIsNull() {
        String url = "/api/notes";
        String content = "Hello I'm its content";
        String failMessage = "Title is required";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(null, content));
        ResponseEntity<String> res = rt // TODO: doc about how a Response Void can contain a body when there was an exception
                .withBasicAuth(username, pass)
                .exchange(url, HttpMethod.POST, note, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);


    }

    @Test
    void shouldNotCreateANoteTitleIsBlank() {
        String url = "/api/notes";
        String title = "  ";
        String content = "Hello I'm its content";
        String failMessage = "Title is required";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<String> res = rt // TODO: doc about how a Response Void can contain a body when there was an exception
                .withBasicAuth(username, pass)
                .exchange(url, HttpMethod.POST, note, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);


    }

    @Test
    void shouldNotCreateANoteTitleLengthIsGreaterThan255() {
        String url = "/api/notes";
        String title = "a".repeat(256);
        String content = "Hello I'm its content";
        String failMessage = "Title must be less than 255 characters";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<String> res = rt // TODO: doc about how a Response Void can contain a body when there was an exception
                .withBasicAuth(username, pass)
                .exchange(url, HttpMethod.POST, note, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);
    }

    @Test
    void shouldNotCreateANoteMustBeAuthenticated() {
        String url = "/api/notes";
        String title = "Hello I'm a title";
        String content = "Hello I'm its content";
        String failMessage = "You must be authenticated to perform this action";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<String> res = rt
                .exchange(url, HttpMethod.POST, note, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);
    }

    @Nested
    class with27Notes {

        static List<String> notesInJson;

        @BeforeAll
        static void beforeAll() throws IOException {
            InputStream is = with27Notes.class.getClassLoader().getResourceAsStream("NotesEntities.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            while (bf.ready()) notesInJson.add(bf.readLine());

            is.close();
            bf.close();
        }

        @BeforeEach
        void setUp() throws IOException {
            // save 27 notes in user already stored
            notesInJson.forEach(note -> {
                try {
                    NoteEntity ne = objectMapper
                            .readerFor(NoteEntity.class)
                            .readValue(note);
                    CreateNoteDTO noteDTO = CreateNoteDTO.builder()
                            .title(ne.getTitle())
                            .content(ne.getContent())
                            .build();
                    HttpEntity<CreateNoteDTO> entity = new HttpEntity<>(noteDTO);
                    rt
                            .withBasicAuth(username, pass)
                            .exchange("/api/notes", HttpMethod.POST, entity, Void.class);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        @Test
        void shouldListAllNotes() throws IOException {

        }


        // delete a note shouldnot delete the user...
        // delete a user should delete all notes...
    }
}