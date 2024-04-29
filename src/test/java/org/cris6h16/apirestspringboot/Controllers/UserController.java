package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserController {
    @Autowired
    TestRestTemplate rt;

    @Test
    void shouldCreateAUser() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "cricri45";
        String email = "cris6h16@gmail.com";

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Get the user
        ResponseEntity<PublicUserDTO> res2 = rt
                .withBasicAuth(username, pass)
                .getForEntity(url + username, PublicUserDTO.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check the user
        PublicUserDTO PUDTO = res2.getBody();
        assertThat(PUDTO).isNotNull();
        assertThat(PUDTO.getUsername()).isEqualTo(username);
        assertThat(PUDTO.getEmail()).isEqualTo(email);
    }


}
