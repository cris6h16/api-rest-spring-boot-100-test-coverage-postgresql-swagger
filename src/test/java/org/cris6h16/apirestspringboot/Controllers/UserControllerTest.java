package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @Autowired
    TestRestTemplate rt;

    @Autowired
    UserRepository userRepository;

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

        // Get the user
        ResponseEntity<PublicUserDTO> res2 = rt
                .withBasicAuth(username, pass)
                .getForEntity(url + "/" + username, PublicUserDTO.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check the user
        PublicUserDTO PUDTO = res2.getBody();
        assertThat(PUDTO).isNotNull();
        assertThat(PUDTO.getUsername()).isEqualTo(username);
        assertThat(PUDTO.getEmail()).isEqualTo(email);
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
        String email = "IReallyDontLikeEcuador";
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



}
