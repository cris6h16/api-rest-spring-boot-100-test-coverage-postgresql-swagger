package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.*;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cris6h16.apirestspringboot.Controllers.Utils.ResponseUtils.getFailBodyMsg;
import static org.cris6h16.apirestspringboot.Controllers.Utils.ResponseUtils.getIdFromLocationHeader;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NoteControllerTest {

    @Autowired
    private TestRestTemplate rt;
    @Autowired
    NoteRepository noteRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper; // deserialize JSON to Java objects

    private static final String username = "cris6h16";
    private static final String pass = "12345678";
    private static final String email = "cristiamherrera21@gmail.com";
    private static Long dbUserID;

    public static final String path = NoteController.path;


    @BeforeEach
    void createUser() { // create just one user
        Optional<UserEntity> u = userRepository.findByUsername(username);
        if (u.isPresent()) return;

        String url = UserController.path;
        CreateUserDTO user = new CreateUserDTO(username, pass, email);
        HttpEntity<CreateUserDTO> entity = new HttpEntity<>(user);
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, entity, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        dbUserID = getIdFromLocationHeader(res);
    }

    @Test
    @DirtiesContext
    @Order(1)
    void shouldCreateANote() {
        String title = "Hello I'm a title";
        String content = "Hello I'm its content";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<Void> res = rt
                .withBasicAuth(username, pass)
                .exchange(path, HttpMethod.POST, note, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //get id from res
        Long id = getIdFromLocationHeader(res);

        // Get the note
        Optional<NoteEntity> noteEntity = noteRepository.findById(id);
        assertThat(noteEntity.isPresent()).isTrue();

        // Check the note
        NoteEntity fromDB = noteEntity.get();
        assertThat(fromDB.getId()).isGreaterThan(0);
        assertThat(fromDB.getCreatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
        assertThat(fromDB.getUpdatedAt()).isNull();
        assertThat(fromDB.getTitle()).isEqualTo(title);
        assertThat(fromDB.getContent()).isEqualTo(content);
    }

    @Test
    @Order(2)
    void shouldNotCreateANoteTitleIsNull() {
        String content = "Hello I'm its content";
        String failMessage = Cons.Note.Validations.TITLE_IS_BLANK_MSG; // "Title is required";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(null, content));
        ResponseEntity<String> res = rt
                .withBasicAuth(username, pass)
                .exchange(path, HttpMethod.POST, note, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualTo(failMessage);
    }

    @Test
    @Order(3)
    void shouldNotCreateANoteTitleIsBlank() {
        String title = "  ";
        String content = "Hello I'm its content";
        String failMessage = Cons.Note.Validations.TITLE_IS_BLANK_MSG; //"Title is required";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<String> res = rt
                .withBasicAuth(username, pass)
                .exchange(path, HttpMethod.POST, note, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualTo(failMessage);
    }

    @Test
    @Order(4)
    void shouldNotCreateANoteTitleLengthIsGreaterThan255() {
        String title = "a".repeat(256);
        String content = "Hello I'm its content";
        String failMessage = Cons.Note.Validations.TITLE_MAX_LENGTH_MSG; //"Title must be less than 255 characters";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<String> res = rt
                .withBasicAuth(username, pass)
                .exchange(path, HttpMethod.POST, note, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualTo(failMessage);
    }

    @Test
    @Order(5)
    void shouldNotCreateANoteMustBeAuthenticated() {
        String title = "Hello I'm a title";
        String content = "Hello I'm its content";

        // Create a note
        HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
        ResponseEntity<Void> res = rt
                .exchange(path, HttpMethod.POST, note, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        //todo: doc about some fail messages are not necessary, response codes has implied meaning
    }

    @Nested
    class with27Notes {

        @Autowired
        Environment env;
        static List<Long> notesIDs = new java.util.ArrayList<>();
        static List<String> notesInJson = new java.util.ArrayList<>();
        static boolean asu; // notes already stored in user


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

            // save 27 notes, in user which is already stored
            notesInJson.forEach(note -> {
                try {
                    NoteEntity ne = objectMapper // in the file, is formatted as a NoteEntity
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
                    notesIDs.add(getIdFromLocationHeader(res));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            asu = true;
        }

        @Test
        @Order(6)
        void shouldNotListAllNotesIsPageable() throws IOException, URISyntaxException {

            Integer elements = notesInJson.size();
            // type reference for the response entity
            ParameterizedTypeReference<List<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<List<PublicNoteDTO>> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(path, HttpMethod.GET, null, responseType);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check the response -> shouldn't be all the notes
            List<PublicNoteDTO> notes = responseEntity.getBody();
            assertThat(notes.size()).isLessThan(elements - 1);// -1 for be sure that the elements are less than the total
        }

        @Test
        void shouldListAllNotesIsPageable_ASC_DESC() {
            String pageParam = env.getProperty("spring.data.web.pageable.page-parameter", String.class);
            String sizeParam = env.getProperty("spring.data.web.pageable.size-parameter", String.class);
            byte size = 7;
            byte page = 1;

            // DESC
            while (true) {
                // --> /api/notes?page=1&size=7&sort=id,desc
                URI uri = UriComponentsBuilder.fromUriString(path)
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

                // check if the response(List) doesn't exceed the page size
                List<PublicNoteDTO> notes = responseEntity.getBody();
                assertThat(notes.size()).isLessThanOrEqualTo(size);

                // check if the notes are sorted in descending order
                Long[] ids = notes.stream().map(PublicNoteDTO::getId).toArray(Long[]::new);
                assertThat(ids).isSortedAccordingTo(Comparator.reverseOrder());

                // if less than size, then is the last page
                if (notes.size() < size) break;
            }

            // ASC
            while (true) {
                // --> /api/notes?page=1&size=7&sort=id,asc
                URI uri = UriComponentsBuilder.fromUriString(path)
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

                // check if the response(List) doesn't exceed the page size
                List<PublicNoteDTO> notes = responseEntity.getBody();
                assertThat(notes.size()).isLessThanOrEqualTo(size);

                // check if the notes are sorted in ascending order
                Long[] ids = notes.stream().map(PublicNoteDTO::getId).toArray(Long[]::new);
                assertThat(ids).isSortedAccordingTo(Comparator.naturalOrder());

                // if less than size, then is the last page
                if (notes.size() < size) break;
            }

        }

        @Test
        void shouldNotListAllNotesIsNotAuthenticated() {
            // type reference for the response entity
            ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<String> re = rt
                    .exchange(path, HttpMethod.GET, null, responseType);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void URIWithNoParamsShouldListInDefaultPageableConfiguration() {
            Integer size = env.getProperty("spring.data.web.pageable.default-page-size", Integer.class);

            ParameterizedTypeReference<List<PublicNoteDTO>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<List<PublicNoteDTO>> responseEntity = rt
                    .withBasicAuth(username, pass)
                    .exchange(path, HttpMethod.GET, null, responseType);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check the response size
            List<PublicNoteDTO> notes = responseEntity.getBody();
            assertThat(notes.size()).isEqualTo(size);
        }


        @Test
        void shouldGetANote() {
            String url = path + "/" + notesIDs.getFirst();

            // Get the note
            ResponseEntity<PublicNoteDTO> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.GET, null, PublicNoteDTO.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.OK);

            // get from db
            Optional<NoteEntity> noteEntity = noteRepository.findById(notesIDs.getFirst());
            assertThat(noteEntity.isPresent()).isTrue();
            NoteEntity fromDB = noteEntity.get();

            // Check the noteFromDB == noteFromHTTP
            PublicNoteDTO noteDTO = re.getBody();
            assertThat(noteDTO.getId()).isEqualTo(fromDB.getId());
            assertThat(noteDTO.getTitle()).isEqualTo(fromDB.getTitle());
            assertThat(noteDTO.getContent()).isEqualTo(fromDB.getContent());
        }

        @Test
        void shouldNotGetANoteIsNotAuthenticated() {
            String url = path + "/" + notesIDs.getFirst();
            // Get the note
            ResponseEntity<String> re = rt
                    .exchange(url, HttpMethod.GET, null, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void shouldNotGetANoteIsNotFound() {
            String failMessage = Cons.Note.Fails.NOT_FOUND; //"Note not found";
            String url = path + "/" + 999999;

            // Get the note
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.GET, null, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(getFailBodyMsg(re)).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext // im creating a new user
        void shouldNotGetNoteIsNotFoundBecauseIsNotTheOwner() {
            String urlUsers = UserController.path;
            String newUserUsername = "github.com/cris6h16";
            String newUserPass = "12345678";
            String newUserEmail = "cristianmherrera21@gmail.com";
            String failMessage = Cons.Note.Fails.NOT_FOUND; //"Note not found";
            String url = path + "/" + notesIDs.getFirst();

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
            ResponseEntity<String> responseEntity = rt
                    .withBasicAuth(newUserUsername, newUserPass)
                    .exchange(url, HttpMethod.GET, null, String.class);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(getFailBodyMsg(responseEntity)).isEqualTo(failMessage);
        }


        // PUT --> CREATE OR REPLACE
        @Test
        @DirtiesContext
        void shouldCreateANotePUT() {
            String title = "Put note";
            String content = "PUT PUT PUT content";
            long id = 1;

            // get an ID which isn't used
            for (long i = 1; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isEmpty()) break;
            }
            // build the URL
            String url = path + "/" + id;

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, note, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check the noteFromDB == noteSavedByMe
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            assertThat(noteEntity.get().getId()).isEqualTo(id);
            assertThat(noteEntity.get().getTitle()).isEqualTo(title);
            assertThat(noteEntity.get().getContent()).isEqualTo(content);
            assertThat(noteEntity.get().getUpdatedAt()).isNull();
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTTitleIsNull() {
            String content = "Hello I'm its content";
            String failMessage = Cons.Note.Validations.TITLE_IS_BLANK_MSG; //"Title is required";
            long id = 0;

            // get an ID which isn't used
            for (long i = 1; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isEmpty()) break;
            }

            // build the URL
            String url = path + "/" + id;

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(null, content));
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(res)).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTTitleIsBlank() {
            String title = "  ";
            String content = "Hello I'm its content";
            String failMessage = Cons.Note.Validations.TITLE_IS_BLANK_MSG; //"Title is required";
            long id = 0;

            // get an ID which isn't used
            for (long i = 1; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isEmpty()) break;
            }

            // build the URL
            String url = path + "/" + id;

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(res)).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTTitleLengthIsGreaterThan255() {
            String title = "a".repeat(256);
            String content = "Hello I'm its content";
            String failMessage = Cons.Note.Validations.TITLE_MAX_LENGTH_MSG; //"Title must be less than 255 characters";
            long id = 0;

            // get an ID which isn't used
            for (long i = 1; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isEmpty()) break;
            }

            // build the URL
            String url = path + "/" + id;

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(res)).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotCreateANotePUTMustBeAuthenticated() {
            String title = "Hello I'm a title";
            String content = "Hello I'm its content";
            String failMessage = Cons.Auth.Fails.UNAUTHENTICATED_MSG; //"You must be authenticated to perform this action";
            long id = 0;

            // get an ID which isn't used
            for (long i = 1; i < Long.MAX_VALUE; i++) {
                id = i;
                if (noteRepository.findById(i).isEmpty()) break;
            }

            // build the URL
            String url = path + "/" + id;

            // PUT a note
            HttpEntity<CreateNoteDTO> note = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> res = rt
                    .exchange(url, HttpMethod.PUT, note, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(res)).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldReplaceANotePUT() {
            String title = "Put note";
            String content = "PUT PUT PUT content";
            long id = notesIDs.getFirst();

            // build the URL
            String url = path + "/" + id;

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // NEW CONTENT & TITLE
            title = "Put note new";
            content = "PUT PUT PUT content new";

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<Void> putRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, putNoteEn, Void.class);
            assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // if note was replaced(PUT -> `id` didn't change)
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            assertThat(noteEntity.get().getId()).isEqualTo(id);
            assertThat(noteEntity.get().getTitle()).isEqualTo(title);
            assertThat(noteEntity.get().getContent()).isEqualTo(content);
            assertThat(noteEntity.get().getUpdatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
        }


        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTTitleIsNull() {
            String content = "Hello I'm its content";
            String failMessage = Cons.Note.Validations.TITLE_IS_BLANK_MSG; //"Title is required";
            long id = notesIDs.getFirst();

            // build the URL
            String url = path + "/" + id;

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // PUT a note
            HttpEntity<CreateNoteDTO> entity = new HttpEntity<>(new CreateNoteDTO(null, content));
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, entity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(re)).isEqualTo(failMessage);

            // check if the note wasn't replaced
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            NoteEntity n = noteEntity.get();
            assertThat(n.getId()).isEqualTo(id);
            assertThat(n.getTitle()).isNotNull();
            assertThat(n.getContent()).isNotEqualTo(content);
        }

        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTTitleIsBlank() {
            String title = "  ";
            String content = "Hello I'm its content";
            String failMessage = Cons.Note.Validations.TITLE_IS_BLANK_MSG; //"Title is required";
            long id = notesIDs.getFirst();

            // build the URL
            String url = path + "/" + id;

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, putNoteEn, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(re)).isEqualTo(failMessage);

            // check if the note wasn't replaced
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            NoteEntity n = noteEntity.get();
            assertThat(n.getId()).isEqualTo(id);
            assertThat(n.getTitle()).isNotEqualTo(title);
            assertThat(n.getContent()).isNotEqualTo(content);
        }

        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTTitleLengthIsGreaterThan255() {
            String title = "a".repeat(256);
            String content = "Hello I'm its content";
            String failMessage = Cons.Note.Validations.TITLE_MAX_LENGTH_MSG; //"Title must be less than 255 characters";
            long id = notesIDs.getFirst();

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // build the URL
            String url = path + "/" + id;

            // PUT a note
            HttpEntity<CreateNoteDTO> putNoteEn = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> putRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PUT, putNoteEn, String.class);
            assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(putRes)).isEqualTo(failMessage);

            // check if the note wasn't replaced
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            NoteEntity n = noteEntity.get();
            assertThat(n.getId()).isEqualTo(id);
            assertThat(n.getTitle()).isNotEqualTo(title);
            assertThat(n.getContent()).isNotEqualTo(content);
        }

        @Test
        @DirtiesContext
        void shouldNotReplaceANotePUTMustBeAuthenticated() {
            String title = "Hello I'm a title";
            String content = "Hello I'm its content";
            String failMessage = Cons.Auth.Fails.UNAUTHENTICATED_MSG; //"You must be authenticated to perform this action";
            long id = notesIDs.getFirst();

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // build the URL
            String url = path + "/" + id;

            // PUT a note
            HttpEntity<CreateNoteDTO> entity = new HttpEntity<>(new CreateNoteDTO(title, content));
            ResponseEntity<String> re = rt
                    .exchange(url, HttpMethod.PUT, entity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(re)).isEqualTo(failMessage);

            // check if the note wasn't replaced
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isTrue();
            NoteEntity n = noteEntity.get();
            assertThat(n.getId()).isEqualTo(id);
            assertThat(n.getTitle()).isNotEqualTo(title);
            assertThat(n.getContent()).isNotEqualTo(content);
        }


        // DELETE --> http request

        @Test
        @DirtiesContext
        void shouldDeleteANote() {
            long id = notesIDs.getFirst();

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // build the URL
            String url = path + "/" + id;

            // DELETE a note
            ResponseEntity<Void> delRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.DELETE, null, Void.class);
            assertThat(delRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // note shouldn't deleted
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isFalse();
        }

        @Test
        @DirtiesContext
        void deleteANoteShouldNotDeleteTheUser() {
            long id = notesIDs.getFirst();

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // build the URL
            String url = path + "/" + id;

            // DELETE a note
            ResponseEntity<Void> delRes = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.DELETE, null, Void.class);
            assertThat(delRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // note should be deleted
            Optional<NoteEntity> noteEntity = noteRepository.findById(id);
            assertThat(noteEntity.isPresent()).isFalse();

            // later delete a note, the user should still exist
            Optional<UserEntity> userEntity = userRepository.findByUsername(username); // is UNIQUE
            assertThat(userEntity.isPresent()).isTrue();
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteANoteIsNotAuthenticated() {
            String failMessage = Cons.Auth.Fails.UNAUTHENTICATED_MSG; //"You must be authenticated to perform this action";
            long id = notesIDs.getFirst();

            // we're sure that the note exists
            assertThat(noteRepository.findById(id).isPresent()).isTrue();

            // build the URL
            String url = path + "/" + id;

            // DELETE a note
            ResponseEntity<String> re = rt
                    .exchange(url, HttpMethod.DELETE, null, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(re)).isEqualTo(failMessage);

            // it wasn't be deleted
            assertThat(noteRepository.findById(id).isPresent()).isTrue();
        }


        @Test
        @DirtiesContext
        void shouldNotDeleteANoteIsNotFound() {
            String failMessage = Cons.Note.Fails.NOT_FOUND; //"Note not found";
            long id = 9397131949L;

            // verify that the note doesn't exist
            assertThat(noteRepository.findById(id).isPresent()).isFalse();

            // build the URL
            String url = path + "/" + id;

            // DELETE a note
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.DELETE, null, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(getFailBodyMsg(re)).isEqualTo(failMessage);
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteANoteIsNotTheOwnerNotFound() {
            String url = path + "/" + notesIDs.getFirst();
            String usrPath = NoteController.path;
            String failMessage = Cons.Note.Fails.NOT_FOUND; //"Note not found";

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
                    .exchange(usrPath, HttpMethod.POST, entity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            // try to delete the note which isn't from the new user
            ResponseEntity<String> re = rt
                    .withBasicAuth(newUserUsername, newUserPass)
                    .exchange(url, HttpMethod.DELETE, null, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(getFailBodyMsg(re)).isEqualTo(failMessage);
        }
    }
}
