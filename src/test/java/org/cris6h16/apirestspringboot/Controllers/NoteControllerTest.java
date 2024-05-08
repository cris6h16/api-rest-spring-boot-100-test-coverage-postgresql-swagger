package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
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
        static List<Long> notesIDs = new java.util.ArrayList<>();
        static List<String> notesInJson = new java.util.ArrayList<>();
        static boolean asu;


        @BeforeAll
        static void beforeAll() throws IOException {

            InputStream is = with27Notes.class.getClassLoader().getResourceAsStream("NotesEntities.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            while (bf.ready()) notesInJson.add(bf.readLine());

            is.close();
            bf.close();

        }

        @BeforeEach
        void saveInDb() throws IOException {
            asu = noteRepository.count() > 0;
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
                    ResponseEntity<Void> res = rt
                            .withBasicAuth(username, pass)
                            .exchange("/api/notes", HttpMethod.POST, entity, Void.class);
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    String[] parts = res.getHeaders().getLocation().toString().split("/");
                    notesIDs.add(Long.parseLong(parts[parts.length - 1]));
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
            ParameterizedTypeReference<List<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<List<PublicNoteDTO>> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.GET, null, responseType);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check the response
            List<PublicNoteDTO> notes = responseEntity.getBody();
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
            ParameterizedTypeReference<List<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<List<PublicNoteDTO>> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(uri, HttpMethod.GET, null, responseType);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            // check the response
            List<PublicNoteDTO> notes = responseEntity.getBody();
            assertThat(notes.size()).isEqualTo(size);
        }


        // TODO: docs about how useful is turn to debug mode
        @Test
        @DirtiesContext
        void shouldGetANote() {
            String url = "/api/notes";
            // Get the note
            ResponseEntity<PublicNoteDTO> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + notesIDs.getFirst(), HttpMethod.GET, null, PublicNoteDTO.class);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

            // get from db
            Optional<NoteEntity> noteEntity = noteRepository.findById(notesIDs.getFirst());
            assertThat(noteEntity.isPresent()).isTrue();
            NoteEntity fromDB = noteEntity.get();

            // Check the note
            PublicNoteDTO noteDTO = responseEntity.getBody();
            assertThat(noteDTO.getId()).isEqualTo(fromDB.getId());
            assertThat(noteDTO.getTitle()).isEqualTo(fromDB.getTitle());
            assertThat(noteDTO.getContent()).isEqualTo(fromDB.getContent());
        }

        @Test
        @DirtiesContext
        void shouldNotGetANoteIsNotAuthenticated() {
            String url = "/api/notes";
            String failMessage = "You must be authenticated to perform this action";

            // Get the note
            ResponseEntity<String> responseEntity = rt
                    .exchange(url + "/" + notesIDs.getFirst(), HttpMethod.GET, null, String.class);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(responseEntity.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotGetANoteIsNotFound() {
            String url = "/api/notes";
            String failMessage = "Note not found";

            // Get the note
            ResponseEntity<String> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + 999999, HttpMethod.GET, null, String.class);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(responseEntity.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotGetNoteIsNotFoundBecauseIsNotTheOwner() {
            String urlUsers = "/api/users";
            String url = "/api/notes";
            String newUserUsername = "github.com/cris6h16";
            String newUserPass = "12345678";
            String newUserEmail = "cristianmherrera21@gmail.com";
            String failMessage = "Note not found";

            //create otehr user
            CreateUserDTO u = CreateUserDTO.builder()
                    .username(newUserUsername)
                    .password(newUserPass)
                    .email(newUserEmail)
                    .build();
            HttpEntity<CreateUserDTO> entity = new HttpEntity<>(u);
            ResponseEntity<Void> res = rt
                    .exchange(urlUsers, HttpMethod.POST, entity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            // Get the note
            ResponseEntity<PublicNoteDTO> responseEntity = rt
                    .withBasicAuth(newUserUsername, newUserPass)
                    .exchange(url + "/" + notesIDs.getFirst(), HttpMethod.GET, null, PublicNoteDTO.class);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }


        // PUT --> CREATE OR REPLACE

        @Test
        @DirtiesContext
        void shouldCreateANotePUT() {
            String url = "/api/notes";
            String title = "Put note";
            String content = "PUT PUT PUT content";
            long id = 0;

            // get an ID which isn't used
            for (long i = 0; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isPresent()) break;
            }

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, note, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Get the note
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            assertThat(noteEntity.get().getId()).isEqualTo(id);
            assertThat(noteEntity.get().getTitle()).isEqualTo(title);
            assertThat(noteEntity.get().getContent()).isEqualTo(content);
            assertThat(noteEntity.get().getUpdatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
            assertThat(noteEntity.get().getDeletedAt()).isNull();
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTTitleIsNull() {
            String url = "/api/notes";
            String content = "Hello I'm its content";
            String failMessage = "Title is required";
            long id = 0;

            // get an ID which isn't used
            for (long i = 0; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isPresent()) break;
            }

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(null, content));
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTTitleIsBlank() {
            String url = "/api/notes";
            String title = "  ";
            String content = "Hello I'm its content";
            String failMessage = "Title is required";
            long id = 0;

            // get an ID which isn't used
            for (long i = 0; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isPresent()) break;
            }

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTTitleLengthIsGreaterThan255() {
            String url = "/api/notes";
            String title = "a".repeat(256);
            String content = "Hello I'm its content";
            String failMessage = "Title must be less than 255 characters";
            long id = 0;

            // get an ID which isn't used
            for (long i = 0; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isPresent()) break;
            }

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTMustBeAuthenticated() {
            String url = "/api/notes";
            String title = "Hello I'm a title";
            String content = "Hello I'm its content";
            String failMessage = "You must be authenticated to perform this action";
            long id = 0;

            // get an ID which isn't used
            for (long i = 0; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isPresent()) break;
            }

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> res = rt
                    .exchange(url + "/" + id, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(res.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldReplaceANotePUT() {
            String url = "/api/notes";
            String title = "Put note";
            String content = "PUT PUT PUT content";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // NEW CONTENT & TITLE
            title = "Put note new";
            content = "PUT PUT PUT content new";

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<Void> putRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, putNoteEn, Void.class);
            assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Get the note
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            assertThat(noteEntity.get().getId()).isEqualTo(id);
            assertThat(noteEntity.get().getTitle()).isEqualTo(title);
            assertThat(noteEntity.get().getContent()).isEqualTo(content);
            assertThat(noteEntity.get().getUpdatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
            assertThat(noteEntity.get().getDeletedAt()).isNull();
        }


        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTTitleIsNull() {
            String url = "/api/notes";
            String content = "Hello I'm its content";
            String failMessage = "Title is required";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(null, content));
            ResponseEntity<String> putRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, putNoteEn, String.class);
            assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(putRes.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTTitleIsBlank() {
            String url = "/api/notes";
            String title = "  ";
            String content = "Hello I'm its content";
            String failMessage = "Title is required";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> putRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, putNoteEn, String.class);
            assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(putRes.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTTitleLengthIsGreaterThan255() {
            String url = "/api/notes";
            String title = "a".repeat(256);
            String content = "Hello I'm its content";
            String failMessage = "Title must be less than 255 characters";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> putRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.PUT, putNoteEn, String.class);
            assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(putRes.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTMustBeAuthenticated() {
            String url = "/api/notes";
            String title = "Hello I'm a title";
            String content = "Hello I'm its content";
            String failMessage = "You must be authenticated to perform this action";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> putRes = rt
                    .exchange(url + "/" + id, HttpMethod.PUT, putNoteEn, String.class);
            assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(putRes.getBody().split("\"")[3]).isEqualTo(failMessage);
        }


        // DELETE --> http request

        @Test
        @DirtiesContext
        void shouldDeleteANote() {
            String url = "/api/notes";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // DELETE a note
            ResponseEntity<Void> delRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, Void.class);
            assertThat(delRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Get the note
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isFalse();
        }

        @Test
        @DirtiesContext
        void deleteANoteShouldNotDeleteTheUser() {
            String url = "/api/notes";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // DELETE a note
            ResponseEntity<Void> delRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, Void.class);
            assertThat(delRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Get the note
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isFalse();

            // Get the user
            Optional<UserEntity> userEntity = userRepository.findByUsername(username); // is UNIQUE
            assertThat(userEntity.isPresent()).isTrue();
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteANoteIsNotAuthenticated() {
            String url = "/api/notes";
            String failMessage = "You must be authenticated to perform this action";
            long id = notesIDs.getFirst();
            //
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // DELETE a note
            ResponseEntity<String> delRes = rt
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, String.class);
            assertThat(delRes.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(delRes.getBody().split("\"")[3]).isEqualTo(failMessage);

            // it wasn't deleted
            assertThat(noteRepository.findById(id).isPresent()).isTrue();
        }



        @Test
        @DirtiesContext
        void shouldNotDeleteANoteIsNotFound() {
            String url = "/api/notes";
            String failMessage = "Note not found";
            long id = 9397131949L;
            //
            assertThat(noteRepository.findById(id).isPresent()).isFalse();

            // DELETE a note
            ResponseEntity<String> delRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, String.class);
            assertThat(delRes.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(delRes.getBody().split("\"")[3]).isEqualTo(failMessage);
        }

        //TODO: doc about the importance of first formating responses, custom responses, etc. because i implemente PUT and DELETE test of NoteTest and I almost pass all the tests in the first try (only one fails)

        @Test
        @DirtiesContext
        void shouldNotDeleteANoteIsNotTheOwnerNotFound() {
            String url = "/api/notes/" + notesIDs.getFirst();
            String failMessage = "Note not found";

            // create another user
            String newUserUsername = "github.com/cris6h16";
            String newUserPass = "12345678";
            String newUserEmail = "cristianmherrera21@gmail.com";
            CreateUserDTO u = CreateUserDTO.builder()
                    .username(newUserUsername)
                    .password(newUserPass)
                    .email(newUserEmail)
                    .build();
            HttpEntity<CreateUserDTO> entity = new HttpEntity<>(u);
            ResponseEntity<Void> res = rt
                    .exchange("/api/users", HttpMethod.POST, entity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            // try to delete the note which isn't from the new user
            ResponseEntity<String> resDelete = rt
                    .withBasicAuth(newUserUsername, newUserPass)
                    .exchange(url, HttpMethod.DELETE, null, String.class);
            assertThat(resDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resDelete.getBody().split("\"")[3]).isEqualTo(failMessage);
        }
    }
}
