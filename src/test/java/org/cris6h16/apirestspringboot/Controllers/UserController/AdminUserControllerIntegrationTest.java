package org.cris6h16.apirestspringboot.Controllers.UserController;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Controller.Path.USER_PATH;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
class AdminUserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService.deleteAll();
        createUsersAndAdmin();
    }



    @Test
    void getPage_successful_Then200_Ok() throws Exception {
        ParameterizedTypeReference<List<PublicUserDTO>> type = new ParameterizedTypeReference<>() {
        };

        URI uri = UriComponentsBuilder.fromPath(USER_PATH)
                .queryParam("page", 0)
                .queryParam("size", 25)
                .queryParam("sort", "email,desc")
                .build().toUri();

        ResponseEntity<List<PublicUserDTO>> list = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .exchange(uri, HttpMethod.GET, null, type);

        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatCollection(list.getBody()).hasSize(24);
        assertThat(list.getBody()).isSortedAccordingTo(Comparator.comparing(PublicUserDTO::getEmail).reversed());
    }

    private void createUsersAndAdmin() {
        for (int i = 0; i < 23; i++) {
            userService.create(
                    CreateUserDTO.builder()
                            .username("cris6h16" + i)
                            .email("cris6h16" + i + "@gmail.com")
                            .password("password" + i)
                            .build()
            );
        }

        userService.createAdmin(
                CreateUserDTO.builder()
                        .username("cris6h16")
                        .email("cristianmherrera21@gmail.com")
                        .password("12345678")
                        .build()
        );
    }
}

