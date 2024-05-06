package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

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
    @DirtiesContext
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
    @DirtiesContext
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
    @DirtiesContext
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
    @DirtiesContext
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
    @DirtiesContext
    class with27Notes {

        @Autowired
        Environment env;

        static List<String> notesInJson;
        static boolean asu = false;

        @BeforeAll
        static void beforeAll() throws IOException {
            notesInJson = new java.util.ArrayList<>();

            InputStream is = with27Notes.class.getClassLoader().getResourceAsStream("NotesEntities.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            while (bf.ready()) notesInJson.add(bf.readLine());

            is.close();
            bf.close();

        }

        @BeforeEach
        void saveInDb() throws IOException {
            if (asu) return;

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

            asu = true;
        }

        @Test
        @DirtiesContext
        void shouldNotListAllNotesIsPageable() throws IOException, URISyntaxException {
            String url = "/api/notes";

            Integer elements = notesInJson.size();
            // type reference for the response entity
            ParameterizedTypeReference<Set<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<Set<PublicNoteDTO>> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.GET, null, responseType);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check the response
            Set<PublicNoteDTO> notes = responseEntity.getBody();
            assertThat(notes.size()).isLessThan(elements - 1);// -1 for be sure that the elements are less than the total
        }

        @Test
        @DirtiesContext
        void shouldListAllNotesIsPageable_ASC_DESC() {
            String url = "/api/notes";
            String pageParam = env.getProperty("spring.data.web.pageable.page-parameter", String.class);
            String sizeParam = env.getProperty("spring.data.web.pageable.size-parameter", String.class);
            byte size = 7;
            byte page = 1;

            // DESC
            while (true) {
                URI uri = UriComponentsBuilder.fromUriString(url)
                        .queryParam(pageParam, page++)
                        .queryParam(sizeParam, size)
                        .queryParam("sort", "id,desc")
                        .build()
                        .toUri();
                ParameterizedTypeReference<List<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
                };
                ResponseEntity<List<PublicNoteDTO>> responseEntity = rt
                        .withBasicAuth(username, pass)
                        .exchange(uri, HttpMethod.GET, null, responseType);
                assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

                List<PublicNoteDTO> notes = responseEntity.getBody();
                assertThat(notes.size()).isLessThanOrEqualTo(size);
                Long[] ids = notes.stream().map(PublicNoteDTO::getId).toArray(Long[]::new);
                assertThat(ids).isSortedAccordingTo(Comparator.reverseOrder());
                // if less than size, then is the last page
                if (notes.size() < size) break;
            }

            // ASC
            while (true) {
                URI uri = UriComponentsBuilder.fromUriString(url)
                        .queryParam(pageParam, page++)
                        .queryParam(sizeParam, size)
                        .queryParam("sort", "id,asc")
                        .build()
                        .toUri();
                ParameterizedTypeReference<List<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
                };
                ResponseEntity<List<PublicNoteDTO>> responseEntity = rt
                        .withBasicAuth(username, pass)
                        .exchange(uri, HttpMethod.GET, null, responseType);
                assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

                List<PublicNoteDTO> notes = responseEntity.getBody();
                assertThat(notes.size()).isLessThanOrEqualTo(size);
                Long[] ids = notes.stream().map(PublicNoteDTO::getId).toArray(Long[]::new);
                assertThat(ids).isSortedAccordingTo(Comparator.naturalOrder());
                // if less than size, then is the last page
                if (notes.size() < size) break;
            }

        }

        @Test
        @DirtiesContext
        void shouldNotListAllNotesIsNotAuthenticated() {
            String url = "/api/notes";
            String failMessage = "You must be authenticated to perform this action";

            // type reference for the response entity
            ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<String> responseEntity = rt
                    .exchange(url, HttpMethod.GET, null, responseType);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(responseEntity.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void URIWithNoParamsShouldListInDefaultPageableConfiguration() {
            String url = "/api/notes";
            Integer size = env.getProperty("spring.data.web.pageable.default-page-size", Integer.class);

            URI uri = UriComponentsBuilder.fromUriString(url)
                    .build()
                    .toUri();
            ParameterizedTypeReference<Set<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<Set<PublicNoteDTO>> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(uri, HttpMethod.GET, null, responseType);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            // check the response
            Set<PublicNoteDTO> notes = responseEntity.getBody();
            assertThat(notes.size()).isEqualTo(size);
        }


        // delete a note shouldnot delete the user...
        // delete a user should delete all notes...
    }
}
