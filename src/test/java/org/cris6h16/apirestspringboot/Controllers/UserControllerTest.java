package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
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
    public final CreateUpdateUserDTO forCreation;

    public UserControllerTest() {
        this.forCreation = CreateUpdateUserDTO.builder()
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
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(forCreation);
        ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get id from Location Header
        Long id = getIdFromLocationHeader(res);

        // Get the user
        Optional<UserEntity> userEntity = userRepository.findById(id);
        assertThat(userEntity.isPresent()).isTrue();


        // Check the user
        UserEntity fromDB = userEntity.get();
        assertThat(fromDB.getId()).isNotNull();
        assertThat(fromDB.getUpdatedAt()).isNull();
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
    void shouldNotCreateAUser_usernameAlreadyExists_CONFLICT() {

        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Constrains.USERNAME_UNIQUE_MSG; // now: "Username already exists";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(forCreation);
        ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Wheres the verification if was it created? --> We already test it in the previous test (good practice)

        // Create the same username
        user = new HttpEntity<>(new CreateUpdateUserDTO(username, (pass + "hello"), ("word" + email)));
        ResponseEntity<String> sameUsernameRes = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(sameUsernameRes.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(getFailBodyMsg(sameUsernameRes)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailAlreadyExists_CONFLICT() {
        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Constrains.EMAIL_UNIQUE_MSG; //"Email already exists";


        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(forCreation);
        ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create the same email
        user = new HttpEntity<>(new CreateUpdateUserDTO((username + "hello"), (pass + "hello"), email));
        ResponseEntity<String> resp = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(getFailBodyMsg(resp)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordTooShort_BAD_REQUEST() {
        String username = forCreation.getUsername();
        String pass = "1234567";
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG;

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(new CreateUpdateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }


    @Test
    @DirtiesContext
    void shouldNotCreateAUser_UsernameIsTooLong_BAD_REQUEST() {
        String username = "a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1);
        String pass = "12345678";
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.USERNAME_MAX_LENGTH_MSG;

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(new CreateUpdateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsInvalid_BAD_REQUEST() {

        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String email = "thisisaninvalidemail";
        String failBodyMssg = Cons.User.Validations.EMAIL_INVALID_MSG; //"Email is invalid"

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(new CreateUpdateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualTo(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsNullOrEmpty_BAD_REQUEST() {
        String username = forCreation.getUsername();
        String pass = forCreation.getPassword();
        String failBodyMssg = Cons.User.Validations.EMAIL_IS_BLANK_MSG; // "Email is required"

        // count users -> before
        long countB = userRepository.count();

        // email is empty
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(new CreateUpdateUserDTO(username, pass, ""));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // email is null
        user = new HttpEntity<>(new CreateUpdateUserDTO(username, pass, null));
        res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_UsernameIsNullOrEmpty_BAD_REQUEST() {
        String pass = forCreation.getPassword();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.USERNAME_IS_BLANK_MSG; // "Username mustn't be blank"

        // count users -> before
        long countB = userRepository.count();

        // username is empty
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(new CreateUpdateUserDTO("", pass, email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // username is null
        user = new HttpEntity<>(new CreateUpdateUserDTO(null, pass, email));
        res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordIsNullOrEmpty_BAD_REQUEST() {
        String username = forCreation.getUsername();
        String email = forCreation.getEmail();
        String failBodyMssg = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG; // "Password must be at least 8 characters"

        // count users -> before
        long countB = userRepository.count();

        // password is empty
        HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(new CreateUpdateUserDTO(username, "", email));
        ResponseEntity<String> res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // password is null
        user = new HttpEntity<>(new CreateUpdateUserDTO(username, null, email));
        res = rt.exchange(path, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getFailBodyMsg(res)).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Nested
    class withAUserInDB {

        UserEntity before = null;// pass is encrypted
        Long id;

        @BeforeEach
        void setUp() {
            if (before != null || userRepository.findByUsername(forCreation.getUsername()).isPresent()) return;

            HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(forCreation);
            ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long id = getIdFromLocationHeader(res);
            before = userRepository.findById(id).get();

            rt // PATCH isn't supported by TestRestTemplate
                    .getRestTemplate()
                    .setRequestFactory(new HttpComponentsClientHttpRequestFactory()); //remember add dependency: org.apache.httpcomponents.client5:httpclient5
            this.id = id;
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsername_NO_CONTENT() {
            CreateUpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();
            //

            forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername("github.com/cris6h16");

            // update `username`
            HttpEntity<CreateUpdateUserDTO> en = new HttpEntity<>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, en, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `username` was updated
            updated = userRepository.findById(id).get();
            assertThat(updated.getUsername()).isEqualTo(forUPDT.getUsername());

            {   // restore the old values for comparison
                updated.setUsername(before.getUsername());
                updated.setUpdatedAt(before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateEmail_NO_CONTENT() {
            CreateUpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for update `email`
            forUPDT = new CreateUpdateUserDTO();
            forUPDT.setEmail("githubcomcris6h16@gmail.com");

            // update `email`
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `email` was updated
            updated = userRepository.findById(id).get();
            assertThat(updated.getEmail()).isEqualTo(forUPDT.getEmail());

            {   // restore the old values for comparison
                updated.setEmail(before.getEmail());
                updated.setUpdatedAt(before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdatePassword_NO_CONTENT() {
            CreateUpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for update `password`
            forUPDT = new CreateUpdateUserDTO();
            forUPDT.setPassword("this-new-pass-has-more-than-8-characters");

            // update `password`
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `password` was updated
            updated = userRepository.findById(id).get();
            assertThat(passwordEncoder.matches(forUPDT.getPassword(), updated.getPassword())).isTrue();

            {   // restore the old values for comparison
                updated.setPassword(before.getPassword());
                updated.setUpdatedAt(before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsernameEmailPassword_NO_CONTENT() {
            CreateUpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for update `username`, `email` & `password`
            forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername("newusername");
            forUPDT.setEmail("newemail@gmail.com");
            forUPDT.setPassword("newpassword");

            // update `username`, `email` & `password`
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `username`, `email` & `password` were updated
            updated = userRepository.findById(id).get();
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
            CreateUpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();


            // DTO for updated `username`
            forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername("github.com/cris6h16");

            // check if `updatedAt` is null
            assertThat(before.getUpdatedAt()).isNull();

            //update: `username`
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `updatedAt` was updated
            updated = userRepository.findById(id).get();
            assertThat(updated.getUpdatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
        }

        @Test
        @DirtiesContext
        void updateShouldNotChange_CreatedAtRolesNotes() {
            CreateUpdateUserDTO forUPDT;
            UserEntity updated;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`, `email` & `password`
            forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername("github.com/cris6h16");
            forUPDT.setEmail("githubcomcris6h16@gmail.com");
            forUPDT.setPassword("githubcomcris6h16");

            // update: `username`, `email` & `password`
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // get from DB & check if `createdAt`, `roles` & `notes` are the same
            updated = userRepository.findById(id).get();
            assertThat(updated.getCreatedAt()).isEqualTo(before.getCreatedAt());
            assertThat(updated.getRoles()).isEqualTo(before.getRoles());
//            assertThat(updated.getNotes()).isEqualTo(before.getNotes());  --> LAZY

        }

        @Test
        @DirtiesContext
        void shouldNotUpdateUsernameAlreadyExists_CONFLICT() {
            String failBodyMssg = Cons.User.Constrains.USERNAME_UNIQUE_MSG; //"Username already exists"
            assertThat(userRepository.findById(id)).isPresent();

            //create a new user
            CreateUpdateUserDTO forCreation2 = CreateUpdateUserDTO.builder()
                    .username("cris6h16_2")
                    .password("12345678")
                    .email("cris6h16_2@gmail.com")
                    .build();
            HttpEntity<CreateUpdateUserDTO> user = new HttpEntity<>(forCreation2);
            ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, user, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername(forCreation2.getUsername());
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);

            // update: `username`
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // get from DB & check if all values are the same
            UserEntity updated = userRepository.findById(id).get();
            assertThat(updated).isEqualTo(before);
        }


        @Test
        @DirtiesContext
        void shouldNotUpdateEmailAlreadyExists_CONFLICT() {
            String failBodyMssg = Cons.User.Constrains.EMAIL_UNIQUE_MSG; // "Email already exists"

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `email`
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            forUPDT.setEmail(before.getEmail());

            // update: `email`
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // get from DB & check if all values are the same
            UserEntity updated = userRepository.findById(id).get();
            assertThat(updated).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouNeedToBeAuthenticated_UNAUTHORIZED() {
            // path/{id}
            String url = path + "/" + id;

            // DTO for updated `username`
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername("github.com/cris6h16");

            // try to update
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt.exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

            // get from DB & check if all values are the same
            UserEntity updated = userRepository.findById(id).get();
            assertThat(updated).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouCannotUpdateOtherUserAccount_FORBIDDEN() {
            Long id = this.id + 1;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername("other-username");

            // try to update
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        }

        @Test
        @DirtiesContext
        void shouldNotUpdateNonexistentId_FORBIDDEN() {
            Long id = this.id + 99999;

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `username`
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            forUPDT.setUsername("githubcom//cris6h16");

            // try to update
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            // check if really doesn't exist a user with that id
            assertThat(userRepository.findById(id)).isNotPresent();
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateEmailIsInvalid_BAD_REQUEST() {
            String failBodyMssg = Cons.User.Validations.EMAIL_INVALID_MSG; //"Email is invalid"

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `email`
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            forUPDT.setEmail("thisisaninvalidemail");

            // try to update
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // check if fromDB == before_to_try_update

            assertThat(userRepository.findById(id).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdatePasswordTooShort_BAD_REQUEST() {
            String failBodyMssg = Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG; //"Password must be at least 8 characters"

            // from user in DB
            String url = path + "/" + id;
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // DTO for updated `password`
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            forUPDT.setPassword("1234567");

            // try to update
            HttpEntity<CreateUpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(getFailBodyMsg(re)).isEqualToIgnoringCase(failBodyMssg);

            // check if fromDB == before_to_try_update

            assertThat(userRepository.findById(id).get()).isEqualTo(before);
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
        }

        @Test
        @DirtiesContext
        void shouldNotGetIsNotAuthenticated_UNAUTHORIZED() {
            String url = path + "/" + id;
            ResponseEntity<String> res = rt
                    .getForEntity(url, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DirtiesContext
        void shouldNotGetUserYouAreNotTheOwner_FORBIDDEN() {
            String failBodyMssg = Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG; //"You aren't the owner of this id";

            // from user in DB
            String url = path + "/" + (id + 1);
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // try to get
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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
        void shouldNotDeleteAUserIsNotAuthenticated_UNAUTHORIZED() {
            // we're sure that the user exists
            assertThat(userRepository.findById(id)).isPresent();

            // from user in DB
            String url = path + "/" + id;

            // try to delete
            ResponseEntity<String> res = rt
                    .exchange(url, HttpMethod.DELETE, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

            // check if user wasn't deleted
            assertThat(userRepository.findById(id)).isPresent();
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteAUserYouAreNotTheOwner_FORBIDDEN() throws JsonProcessingException {
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
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            // no user should have been deleted
            assertThat(userRepository.count()).isEqualTo(countB);
        }

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
        void loginBadCredentials_UNAUTHORIZED() {

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
        void passingAStrInsteadOfANumberShouldReturn_BAD_REQUEST() {
            String username = forCreation.getUsername();
            String pass = forCreation.getPassword();

            // PATCH
            String url = path + "/string";
            CreateUpdateUserDTO forUPDT = new CreateUpdateUserDTO();
            HttpEntity<CreateUpdateUserDTO> entity = new HttpEntity<>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange(url, HttpMethod.PATCH, entity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // GET /{id}
            url = path + "/string";
            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            //
        }

    }

    @Nested
    @DirtiesContext
    class with27Users1AdminInDB {

        String adminUsername = "cris6h16InGithub";
        String adminPass = "cris6h16";
        static List<Long> userIds;
        static List<CreateUpdateUserDTO> usersToCreate;
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
                    CreateUpdateUserDTO user = objectMapper
                            .readerFor(CreateUpdateUserDTO.class)
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
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
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
            }

            // create users
            if (!(userRepository.count() == 27 + 1)) {
                for (CreateUpdateUserDTO user : usersToCreate) {
                    HttpEntity<CreateUpdateUserDTO> en = new HttpEntity<>(user);
                    ResponseEntity<Void> res = rt.exchange(path, HttpMethod.POST, en, Void.class);
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    userIds.add(getIdFromLocationHeader(res));
                }
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
//            UserEntity ue = userRepository.findById(ids.getFirst()).get();
//            later: rt.withBasicAuth(ue.getUsername(), ue.getPassword()) -> X password is encrypted --> this stolen to me a lot of time until I realize it
            ResponseEntity<String> res = rt
                    .withBasicAuth(usersToCreate.getFirst().getUsername(), usersToCreate.getFirst().getPassword())
                    .exchange(path, HttpMethod.GET, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }


    }
}





