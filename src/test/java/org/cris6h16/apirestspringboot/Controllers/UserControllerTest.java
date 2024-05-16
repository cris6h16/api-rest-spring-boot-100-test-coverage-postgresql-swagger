package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.UpdateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cris6h16.apirestspringboot.Controllers.Utils.ResponseUtils.getFailBodyMsg;
import static org.cris6h16.apirestspringboot.Controllers.Utils.ResponseUtils.getIdFromLocationHeader;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @Autowired
    TestRestTemplate rt;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ObjectMapper objectMapper; // for parsing JSON

    @Autowired
    NoteRepository noteRepository;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder; // for comparing passwords

    public static final String path = "/api/users";
    public final CreateUserDTO forCreation;

    public UserControllerTest() {
        this.forCreation = CreateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build();
    }

    @Test
    @DirtiesContext
    void shouldCreateAUser() {
        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String email = forCreation.getEmail();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(forCreation);
        ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get id from Location Header
        Long id = getIdFromLocationHeader(res);

        // Get the user
        Optional<UserEntity> userEntity = userRepository.findByIdEagerly(id);
        assertThat(userEntity.isPresent()).isTrue();


        // Check the user
        UserEntity fromDB = userEntity.get();
//        assertThat(fromDB.getNotes()).isEmpty();   --> LAZY
        assertThat(fromDB.getId()).isNotNull();
        assertThat(fromDB.getUpdatedAt()).isNull();
//        assertThat(fromDB.getDeletedAt()).isNull();
        assertThat(fromDB.getCreatedAt()).isNotNull();
        assertThat(fromDB.getEmail()).isEqualTo(email);
        assertThat(fromDB.getUsername()).isEqualTo(username);
        assertThat(fromDB.getRoles()).size().isEqualTo(1);
        assertThat(passwordEncoder.matches(pass, fromDB.getPassword())).isTrue();
        assertThat(fromDB.getRoles().stream()
                .filter(r -> (r.getName().equals(ERole.ROLE_USER)) && r.getId() > 0)
                .count()).isEqualTo(1);
    }


    @Test
    @DirtiesContext
    void shouldNotCreateAUser_usernameAlreadyExists() {

        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Constrains.USERNAME_UNIQUE_MSG; // now: "Username already exists";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(forCreation);
        ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Wheres the verification if was it created? --> We already test it in the previous test (good practice)

        // Create the same username
        user = new HttpEntity<>(new CreateUserDTO(username, (pass + "hello"), ("word" + email)));
        ResponseEntity<String> sameUsernameRes = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(sameUsernameRes.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(getFailBodyMsg(sameUsernameRes)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailAlreadyExists() {
        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Constrains.EMAIL_UNIQUE_MSG; //"Email already exists";


        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(forCreation);
        ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create the same email
        user = new HttpEntity<>(new CreateUserDTO((username + "hello"), (pass + "hello"), email));
        ResponseEntity<String> resp = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(getFailBodyMsg(resp)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordTooShort() {
        String username = forCreation.getUsername();
        String pass = "1234567";
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG;

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }


    @Test
    @DirtiesContext
    void shouldNotCreateAUser_UsernameIsTooLong() {
        String username = "a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1);
        String pass = "12345678";
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.USERNAME_MAX_LENGTH_MSG;

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsInvalid() {

        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String email = "thisisaninvalidemail";
        String failBodyMssg = Cons.User.Validations.EMAIL_INVALID_MSG; //"Email is invalid"

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualTo(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsNullOrEmpty() {
        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String failBodyMssg = Cons.User.Validations.EMAIL_IS_BLANK_MSG; // "Email is required"

        // count users -> before
        long countB = userRepository.count();

        // email is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, ""));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // email is null
        user = new HttpEntity<>(new CreateUserDTO(username, pass, null));
        res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_UsernameIsNullOrEmpty() {
        String pass = forCreation.getPassword();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.USERNAME_IS_BLANK_MSG; // "Username mustn't be blank"

        // count users -> before
        long countB = userRepository.count();

        // username is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO("", pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // username is null
        user = new HttpEntity<>(new CreateUserDTO(null, pass, email));
        res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordIsNullOrEmpty() {
        String username = forCreation.getUsername();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG; // "Password must be at least 8 characters"

        // count users -> before
        long countB = userRepository.count();

        // password is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, "", email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // password is null
        user = new HttpEntity<>(new CreateUserDTO(username, null, email));
        res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Nested
            //@DirtiesContext // Really I don't understand why some tests fail, it runs like if @DirtiesContext doesn't work in class level
    class withAUserInDB {

        UserEntity before;// pass is encrypted
        Long id;

        @BeforeEach
        void setUp() {
            HttpEntity<CreateUserDTO> user = new HttpEntity<>(forCreation);
            ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long id = getIdFromLocationHeader(res);
            before = userRepository.findByIdEagerly(id).get();

            rt // PATCH isn't supported by TestRestTemplate
                    .getRestTemplate()
                    .setRequestFactory(new HttpComponentsClientHttpRequestFactory()); //remember add dependency: org.apache.httpcomponents.client5:httpclient5

            this.id = id;
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsername() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();
            //

            forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername("github.com/cris6h16");

            // update `username`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `username` was updated
            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated.getUsername()).isEqualTo(forUPDT.getUsername());

            {   // restore the old values for comparison
                updated.setUsername(before.getUsername());
                updated.setUpdatedAt(before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateEmail() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for update `email`
            forUPDT = new UpdateUserDTO(id);
            forUPDT.setEmail("githubcomcris6h16@gmail.com");

            // update `email`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `email` was updated
            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated.getEmail()).isEqualTo(forUPDT.getEmail());

            {   // restore the old values for comparison
                updated.setEmail(before.getEmail());
                updated.setUpdatedAt(before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdatePassword() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for update `password`
            forUPDT = new UpdateUserDTO(id);
            forUPDT.setPassword("this-new-pass-has-more-than-8-characters");

            // update `password`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `password` was updated
            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(passwordEncoder.matches(forUPDT.getPassword(), updated.getPassword())).isTrue();

            {   // restore the old values for comparison
                updated.setPassword(before.getPassword());
                updated.setUpdatedAt(before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsernameEmailPassword() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for update `username`, `email` & `password`
            forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername("newusername");
            forUPDT.setEmail("newemail@gmail.com");
            forUPDT.setPassword("newpassword");

            // update `username`, `email` & `password`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `username`, `email` & `password` were updated
            updated = userRepository.findByIdEagerly(id).get();
            assertThat(updated.getUsername()).isEqualTo(forUPDT.getUsername());
            assertThat(updated.getEmail()).isEqualTo(forUPDT.getEmail());
            assertThat(passwordEncoder.matches(forUPDT.getPassword(), updated.getPassword())).isTrue();

            {   // restore the old values for comparison
                updated.setUsername(before.getUsername());
                updated.setEmail(before.getEmail());
                updated.setPassword(before.getPassword());
                updated.setUpdatedAt(before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldBeGreaterUpdatedAt() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();


            // DTO for updated `username`
            forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername("github.com/cris6h16");

            // check if `updatedAt` is null
            assertThat(before.getUpdatedAt()).isNull();

            //update: `username`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);

            // get from DB & check if `updatedAt` was updated
            updated = userRepository.findByIdEagerly(id).get();
            assertThat(updated.getUpdatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
        }

        @Test
        @DirtiesContext
        void updateShouldNotChange_CreatedAtRolesNotes() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`, `email` & `password`
            forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername("github.com/cris6h16");
            forUPDT.setEmail("githubcomcris6h16@gmail.com");
            forUPDT.setPassword("githubcomcris6h16");

            // update: `username`, `email` & `password`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `createdAt`, `roles` & `notes` are the same
            updated = userRepository.findByIdEagerly(id).get();
            assertThat(updated.getCreatedAt()).isEqualTo(before.getCreatedAt());
            assertThat(updated.getRoles()).isEqualTo(before.getRoles());
//            assertThat(updated.getNotes()).isEqualTo(before.getNotes());  --> LAZY

        }

        @Test
        @DirtiesContext
        void shouldNotUpdateUsernameAlreadyExists() {
            String failBodyMssg = Cons.User.Constrains.USERNAME_UNIQUE_MSG; //"Username already exists"

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername(username);
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);

            // update: `username`
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // get from DB & check if all values are the same
            UserEntity updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated).isEqualTo(before);
        }


        @Test
        @DirtiesContext
        void shouldNotUpdateEmailAlreadyExists() {
            String failBodyMssg = Cons.User.Constrains.EMAIL_UNIQUE_MSG; // "Email already exists"

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `email`
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            forUPDT.setEmail(before.getEmail());

            // update: `email`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // get from DB & check if all values are the same
            UserEntity updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouNeedToBeAuthenticated() {
            String failBodyMssg = Cons.Auth.Fails.UNAUTHENTICATED_MSG;

            // path/{id}
            String url = path + "/" + id;

            // DTO for updated `username`
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername("github.com/cris6h16");

            // try to update
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt.exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // get from DB & check if all values are the same
            UserEntity updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouCannotUpdateOtherUserAccount() {
            String failBodyMssg = Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG; //"You aren't the owner of this id"
            Long id = this.id + 1;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername("other-username");

            // try to update
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

        }

        @Test
        @DirtiesContext
        void shouldNotUpdateNonexistentIdUnauthorized() {
            String failBodyMssg = Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG; //"You aren't the owner of this id"
            Long id = this.id + 99999;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            forUPDT.setUsername("githubcom//cris6h16");

            // try to update
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // check if really doesn't exist a user with that id
            assertThat(userRepository.findByIdEagerly(forUPDT.getId())).isNotPresent();
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateEmailIsInvalid() {
            String failBodyMssg = Cons.User.Validations.EMAIL_INVALID_MSG; //"Email is invalid"

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `email`
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            forUPDT.setEmail("thisisaninvalidemail");

            // try to update
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // check if fromDB == before_to_try_update
            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdatePasswordTooShort() {
            String failBodyMssg = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG; //"Password must be at least 8 characters"

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `password`
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            forUPDT.setPassword("1234567");

            // try to update
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // check if fromDB == before_to_try_update
            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }


        @Test
        @DirtiesContext
        void shouldGetAUserById() {
            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();
            String email = before.getEmail();

            // get user
            ResponseEntity<PublicUserDTO> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url, PublicUserDTO.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check if retrieved user has credentials with which it was retrieved
            PublicUserDTO user = res.getBody();
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getRoles().size()).isEqualTo(1);
            assertThat(user.getNotes()).isEmpty();
        }

        @Test
        @DirtiesContext
// necessary because `@BeforeEach` can lead in CONFLICT (creating the same user)
        void shouldNotGetIsNotAuthenticated() {
            String failBodyMssg = Cons.Auth.Fails.UNAUTHENTICATED_MSG;

            String url = path + "/" + id;

            ResponseEntity<String> res = rt
                    .getForEntity(url, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);
        }

        @Test
        @DirtiesContext
            // necessary because `@BeforeEach` can lead in CONFLICT (creating the same user)
        void shouldNotGetUserYouAreNotTheOwner() {
            String failBodyMssg = Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG; //"You aren't the owner of this ID";

            // from user in DB
            String url = path + "/" + (id + 1);
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // try to get
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);
        }

        @Test
        @DirtiesContext
        void shouldDeleteAUser() {
            // we're sure that the user exists
            assertThat(userRepository.findById(id)).isPresent();

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // delete user
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.DELETE, null, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check if user was deleted
            assertThat(userRepository.findById(id)).isEmpty();
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteAUserIsNotAuthenticated() {
            String failBodyMssg = Cons.Auth.Fails.UNAUTHENTICATED_MSG; //"You must be authenticated to perform this action";

            // we're sure that the user exists
            assertThat(userRepository.findById(id)).isPresent();

            // from user in DB
            String url = path + "/" + id;

            // try to delete
            ResponseEntity<String> res = rt
                    .exchange(url, HttpMethod.DELETE, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

            // check if user wasn't deleted
            assertThat(userRepository.findById(id)).isPresent();
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteAUserYouAreNotTheOwner() throws JsonProcessingException {
            String failBodyMssg = Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG; //"You aren't the owner of this ID";
            Long id = this.id + 1;

            // count users -> before
            long countB = userRepository.count();

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // try to delete
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.DELETE, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

            // no user should have been deleted
            assertThat(userRepository.count()).isEqualTo(countB);
        }
        // If we let multiple sessions & generally it isn't with basic auth...
//        @Test
//        @DirtiesContext
//        void userNotFound() {
//            String failBodyMssg = "User not found";
//            Long id = id;
//
//            ResponseEntity<String> res = rt
//                    .withBasicAuth(username, pass)
//                    .exchange(url + "/" + id, HttpMethod.DELETE, null, String.class);
//            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);
//        }

        @Test
        @DirtiesContext
        void shouldDeleteAllNotesWhenDeleteAUser() {
            String urlNotes = NoteControllerTest.path;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // create notes --> test/resources/NotesEntities.txt => json
            try (
                    InputStream in = getClass().getClassLoader().getResourceAsStream("NotesEntities.txt");
                    BufferedReader bf = new BufferedReader(new InputStreamReader(in));
            ) {
                String line;
                while ((line = bf.readLine()) != null) {
                    NoteEntity ne = objectMapper
                            .readerFor(NoteEntity.class)
                            .readValue(line);
                    CreateNoteDTO cnd = CreateNoteDTO.builder()
                            .title(ne.getTitle())
                            .content(ne.getContent())
                            .build();
                    HttpEntity<CreateNoteDTO> httpEntity = new HttpEntity<>(cnd);
                    ResponseEntity<Void> res = rt
                            .withBasicAuth(username, pass)
                            .exchange(urlNotes, HttpMethod.POST, httpEntity, Void.class);
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // check if notes were created
            assertThat(noteRepository.findByUserId(id)).hasSizeGreaterThan(10);

            // delete user
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.DELETE, null, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check if user was deleted
            assertThat(userRepository.findById(id)).isEmpty();

            // check if notes were deleted
            assertThat(noteRepository.findByUserId(id)).isEmpty();
        }

        @Test
        @DirtiesContext
        void loginBadCredentials() {
//            String failBodyMssg = Cons.Auth.Fails.BAD_CREDENTIALS_MSG; --> a message is not necessary

            // from user in DB
            String url = path + "/1";
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // username wrong
            ResponseEntity<String> res = rt
                    .withBasicAuth(username + "bad", pass)
                    .exchange(url, HttpMethod.GET, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

            // password wrong
            res = rt
                    .withBasicAuth(username, pass + "bad")
                    .exchange(url, HttpMethod.POST, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }



        @Test
        @DirtiesContext
        void passingAStrInsteadOfANumberShouldReturnBadRequest() {
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();
            String failBodyMssg = Cons.Controller.Fails.Argument.DATATYPE_PASSED_WRONG;

            // PATCH
            String url = path + "/string";
            UpdateUserDTO forUPDT = new UpdateUserDTO(id);
            HttpEntity<UpdateUserDTO> entity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, entity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // GET /{id}
            url = path + "/string";
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

            //
        }

    }

    @Nested
    @DirtiesContext
    class with27Users1AdminInDB {

        String adminUsername = "cris6h16InGithub";
        String adminPass = "cris6h16";
        static List<Long> userIds;
        static List<CreateUserDTO> usersToCreate;
        @Autowired
        Environment env;


        @BeforeAll
        static void beforeAll() {
            userIds = new ArrayList<>();
            usersToCreate = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();

            // save CreateUserDTO in static List from .txt
            try (
                    InputStream in = with27Users1AdminInDB.class.getClassLoader().getResourceAsStream("CreateUserDTOs.txt");
                    BufferedReader bf = new BufferedReader(new InputStreamReader(in));
            ) {
                String line;
                while ((line = bf.readLine()) != null) {
                    CreateUserDTO user = objectMapper
                            .readerFor(CreateUserDTO.class)
                            .readValue(line);
                    usersToCreate.add(user);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertThat(usersToCreate).hasSize(27);
        }

        @BeforeEach
        void setUp() {
            //create an admin --> directly in DB
            userRepository.executeInTransaction(() -> {
                RoleEntity role = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(null);
                if (role == null) role = RoleEntity.builder().name(ERole.ROLE_ADMIN).build();

                UserEntity ue = UserEntity.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(adminPass))
                        .email(adminUsername + "@gmail.com")
                        .roles(Set.of(role))
                        .createdAt(new Date(System.currentTimeMillis()))
                        .build();
                userRepository.save(ue);
            });

            // create users
            for (CreateUserDTO user : usersToCreate) {
                HttpEntity<CreateUserDTO> en = new HttpEntity<>(user);
                ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, en, Void.class);
                assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                userIds.add(getIdFromLocationHeader(res));
            }

            // check if all (users + admin) were created
            assertThat(userRepository.count()).isEqualTo(28);// 27 + 1

            // check if admin has ROLE_ADMIN
            Assertions.assertTrue(userRepository.findByUsername(adminUsername).isPresent(),
                    "Admin not found, first insert an Admin in DB");
            Assertions.assertTrue(userRepository.findByUsername(adminUsername).get().getRoles().stream()
                            .anyMatch(r -> r.getName().equals(ERole.ROLE_ADMIN)),
                    "Admin credentials used doesn't have " + ERole.ROLE_ADMIN);
        }


        @Test
        @DirtiesContext
        void shouldListAllUsersInPagesIsPageable() {
            String pageParam = env.getProperty("spring.data.web.pageable.page-parameter");
            String sizeParam = env.getProperty("spring.data.web.pageable.default-page-size");
            short page = 0;
            short size = 3;

            //CREATE ADMIN --> directly in DB

            //DESC
            while (true) {
                ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<List<PublicUserDTO>>() {
                };
                URI uri = UriComponentsBuilder.fromUriString(path)
                        .queryParam(pageParam, page++)
                        .queryParam(sizeParam, size)
                        .queryParam("sort", "id,desc")
                        .build()
                        .toUri();
                ResponseEntity<List<PublicUserDTO>> pu = rt
                        .withBasicAuth(adminUsername, adminPass)
                        .exchange(uri, HttpMethod.GET, null, ptr);
                assertThat(pu.getStatusCode()).isEqualTo(HttpStatus.OK);

                List<PublicUserDTO> listPU = pu.getBody();
                assertThat(listPU.stream()
                        .map(PublicUserDTO::getId)
                        .collect(Collectors.toCollection(() -> new ArrayList<>()))
                ).isSortedAccordingTo(Collections.reverseOrder());
                // if retrieved has less size than the page size, then it's the last page
                if (listPU.size() < size) break;
            }

            //ASC
            while (true) {
                ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<List<PublicUserDTO>>() {
                };
                URI uri = UriComponentsBuilder.fromUriString(path)
                        .queryParam(pageParam, page++)
                        .queryParam(sizeParam, size)
                        .queryParam("sort", "id,asc")
                        .build()
                        .toUri();
                ResponseEntity<List<PublicUserDTO>> pu = rt
                        .withBasicAuth(adminUsername, adminPass)
                        .exchange(uri, HttpMethod.GET, null, ptr);
                assertThat(pu.getStatusCode()).isEqualTo(HttpStatus.OK);

                List<PublicUserDTO> listPU = pu.getBody();
                // if retrieved has less size than the page size, then it's the last page
                if (listPU.size() < size) break;
            }

        }

        @Test
        @DirtiesContext
// necessary because `@BeforeEach` can lead in CONFLICT (creating the same user)
        void shouldNotListAllUserIsPageable() {
            // total users
            Integer users = (int) userRepository.count();

            // list all users
            ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<List<PublicUserDTO>> res = rt
                    .withBasicAuth(adminUsername, adminPass)
                    .exchange(path, HttpMethod.GET, null, ptr);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check if retrieved has less size than the total users(automatic pagination)
            assertThat(res.getBody()).hasSizeLessThan(users);
        }

        @Test
        @DirtiesContext
        void shouldListAllUsersDefaultConfigNotUrlParams() {
            // total users
            Integer users = (int) userRepository.count();
            Short defSize = env.getProperty("spring.data.web.pageable.default-page-size", Short.class);

            // list all users
            ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<List<PublicUserDTO>>() {
            };
            ResponseEntity<List<PublicUserDTO>> res = rt
                    .withBasicAuth(adminUsername, adminPass)
                    .exchange(path, HttpMethod.GET, null, ptr);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

            // check if retrieved has the default size
            assertThat(res.getBody()).hasSize(defSize);
        }

        @Test
        @DirtiesContext
        void shouldNotListAllUserIsNotAdmin() {
            String failBodyMssg = Cons.Auth.Fails.Authority.IS_NOT_ADMIN;
//            UserEntity ue = userRepository.findById(ids.getFirst()).get();
//            later: rt.withBasicAuth(ue.getUsername(), ue.getPassword()) -> X password is encrypted --> this stolen to me a lot of time until I realize it
            ResponseEntity<String> res = rt
                    .withBasicAuth(usersToCreate.getFirst().getUsername(), usersToCreate.getFirst().getPassword())
                    .exchange(path, HttpMethod.GET, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);
        }


    }
}





