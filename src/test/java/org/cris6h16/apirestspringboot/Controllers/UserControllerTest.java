package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.UpdateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest { //TODO: improve HARDCODE

    @Autowired
    TestRestTemplate rt;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder; // for comparing passwords

    @Test
    @DirtiesContext
    void shouldCreateAUser() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get id from Location Header
        String[] parts = res.getHeaders().getLocation().toString().split("/");
        Long id = Long.parseLong(parts[parts.length - 1]);

        // Get the user
        Optional<UserEntity> userEntity = userRepository.findByIdEagerly(id);
        assertThat(userEntity.isPresent()).isTrue();


        // Check the user
        UserEntity fromDB = userEntity.get();
//        assertThat(fromDB.getNotes()).isEmpty();
        assertThat(fromDB.getId()).isNotNull();
        assertThat(fromDB.getUpdatedAt()).isNull();
        assertThat(fromDB.getDeletedAt()).isNull();
        assertThat(fromDB.getCreatedAt()).isNotNull();
        assertThat(fromDB.getEmail()).isEqualTo(email);
        assertThat(fromDB.getUsername()).isEqualTo(username);
        assertThat(fromDB.getRoles()).size().isEqualTo(1);
        assertThat(passwordEncoder.matches(pass, fromDB.getPassword())).isTrue();
        assertThat(fromDB.getRoles()
                .stream()
                .filter(r -> (r.getName().equals(ERole.ROLE_USER)) && r.getId() > 0)
                .count()).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_usernameAlreadyExists() {

        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";
        String failBodyMssg = "Username already exists";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Wheres the verification if was it created? --> We already test it in the previous test (good practice)

        // Create the same username
        user = new HttpEntity<>(new CreateUserDTO(username, (pass + "hello"), ("word" + email)));
        ResponseEntity<String> res2 = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res2.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailAlreadyExists() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";
        String failBodyMssg = "Email already exists";


        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create the same email
        user = new HttpEntity<>(new CreateUserDTO((username + "hello"), (pass + "hello"), email));
        ResponseEntity<String> res2 = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res2.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordTooShort() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "1234567";
        String email = "cristianmherrera21@gmail.com";
        String failBodyMssg = "Password must be at least 8 characters";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsInvalid() {

        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "thisisaninvalidemail";
        String failBodyMssg = "Email is invalid";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualTo(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsNullOrEmpty() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "";
        String failBodyMssg = "Email is required";

        // count users -> before
        long countB = userRepository.count();

        // email is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // email is null
        user = new HttpEntity<>(new CreateUserDTO(username, pass, null));
        res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_UsernameIsNullOrEmpty() {
        String url = "/api/users";
        String username = "";
        String pass = "12345678";
        String email = "cris6h16@gmailcom";
        String failBodyMssg = "Username mustn't be blank";

        // count users -> before
        long countB = userRepository.count();

        // username is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // username is null
        user = new HttpEntity<>(new CreateUserDTO(null, pass, email));
        res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordIsNullOrEmpty() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "";
        String email = "cris6h16@gmail.com";
        String failBodyMssg = "Password must be at least 8 characters";

        // count users -> before
        long countB = userRepository.count();

        // password is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        System.out.println(res);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // password is null
        user = new HttpEntity<>(new CreateUserDTO(username, null, email));
        res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Nested
            //@DirtiesContext // Really I don't understand why some tests fail, it runs like if @DirtiesContext doesn't work in class level
    class withAUserInDB { // TODO: Centralize "hardcoded" & try to avoid it

        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";
        UserEntity before;

        @BeforeEach
        void setUp() {
            // Create a user (we already tested it)
            System.out.println(userRepository.findAllEager());
            HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
            ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long id = Long.parseLong(res.getHeaders().getLocation().toString().split("/")[4]);
            before = userRepository.findByIdEagerly(id).get();


            // PATCH isn't supported by TestRestTemplate
            rt
                    .getRestTemplate()
                    .setRequestFactory(new HttpComponentsClientHttpRequestFactory()); // remember add dependency: org.apache.httpcomponents.client5:httpclient5
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsername() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("github.com/cris6h16");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated.getUsername()).isEqualTo(forUPDT.getUsername());

            {   // restore the old values for comparison
                updated.setUsername((String) before.getUsername());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateEmail() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setEmail("githubcomcris6h16@gmail.com");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated.getEmail()).isEqualTo(forUPDT.getEmail());

            {   // restore the old values for comparison
                updated.setEmail((String) before.getEmail());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdatePassword() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setPassword("iwannagotoliveinusa");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(passwordEncoder.matches(forUPDT.getPassword(), updated.getPassword())).isTrue();

            {   // restore the old values for comparison
                updated.setPassword((String) before.getPassword());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsernameEmailPassword() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("newusername");
            forUPDT.setEmail("newemail@gmail.com");
            forUPDT.setPassword("newpassword");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(before.getId()).get();
            assertThat(updated.getUsername()).isEqualTo(forUPDT.getUsername());
            assertThat(updated.getEmail()).isEqualTo(forUPDT.getEmail());
            assertThat(passwordEncoder.matches(forUPDT.getPassword(), updated.getPassword())).isTrue();


            {   // restore the old values for comparison
                updated.setUsername((String) before.getUsername());
                updated.setEmail((String) before.getEmail());
                updated.setPassword((String) before.getPassword());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }

        }

        @Test
        @DirtiesContext
        void shouldBeGreaterUpdatedAt() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // updated `username`
            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("github.com/cris6h16");

            //first update then `UpdatedAt` pass from `null` -> `Date`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);

            updated = userRepository.findByIdEagerly(before.getId()).get();
            assertThat(updated.getUpdatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
        }

        @Test
        @DirtiesContext
        void updateShouldNotChange_DeletedAtCreatedAtRolesNotes() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // updated `username`, `email` & `password`
            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("newusername");
            forUPDT.setEmail("newemail@gmail.com");
            forUPDT.setPassword("newpassword");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);

            updated = userRepository.findByIdEagerly(before.getId()).get();
            assertThat(updated.getCreatedAt()).isEqualTo(before.getCreatedAt());
            assertThat(updated.getDeletedAt()).isEqualTo(before.getDeletedAt());
            assertThat(updated.getRoles()).isEqualTo(before.getRoles());
//            assertThat(updated.getNotes()).isEqualTo(before.getNotes());

        }

        @Test
        @DirtiesContext
        void shouldNotUpdateUsernameAlreadyExists() {
            String failBodyMssg = "Username already exists";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername(username);
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);

            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg); //TODO: remember doc about the importance of format responses

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }


        @Test
        @DirtiesContext
        void shouldNotUpdateEmailAlreadyExists() {
            String failBodyMssg = "Email already exists";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setEmail(email);

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg); //TODO: remember doc about the importance of format responses

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouNeedToBeAuthenticated() {
            String failBodyMssg = "You need to be authenticated to perform this action";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("github.com/cris6h16");
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);

            ResponseEntity<String> re = rt.exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouCannotUpdateOtherUserAccount() {
            String failBodyMssg = "You aren't the owner of this id";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId() + 1);
            forUPDT.setUsername("other-username");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateNonexistentIdForbidden() {
            String failBodyMssg = "You aren't the owner of this id";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId() + 10);
            forUPDT.setUsername("other-username");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateEmailIsInvalid() {
            String failBodyMssg = "Email is invalid";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setEmail("thisisaninvalidemail");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdatePasswordTooShort() {
            String failBodyMssg = "Password must be at least 8 characters";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setPassword("1234567");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }


        @Test
        @DirtiesContext
            // Others tests can affect the stored in `before`
        void shouldGetAUserById() {
            Long id = before.getId();

            ResponseEntity<PublicUserDTO> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url + "/" + id, PublicUserDTO.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

            PublicUserDTO user = res.getBody();
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getRoles().size()).isEqualTo(1);
            assertThat(user.getNotes()).isEmpty();
        }

        @Test
        void shouldNotGetIsNotAuthenticated() {
            String failBodyMssg = "You need to be authenticated to perform this action";

            Long id = before.getId();

            ResponseEntity<String> res = rt
                    .getForEntity(url + "/" + id, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);
        }

        @Test
        void shouldNotGetUserYouAreNotTheOwner() {
            String failBodyMssg = "You aren't the owner of this ID";

            Long id = before.getId() + 1091;

            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url + "/" + id, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);
        }

    }

    // delete a user should delete all notes...
}



