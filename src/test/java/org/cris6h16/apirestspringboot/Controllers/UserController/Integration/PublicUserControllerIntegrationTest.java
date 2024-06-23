package org.cris6h16.apirestspringboot.Controllers.UserController.Integration;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Controller.Path.USER_PATH;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
class PublicUserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService.deleteAll();
    }

    @Test
    void create_successful_Then201_Created() throws Exception {

        CreateUserDTO dto = CreateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build();

        ResponseEntity<Void> response = this.restTemplate
                .postForEntity(USER_PATH, dto, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        String location = response.getHeaders().getLocation().toString();

        assertThat(location).matches(USER_PATH + "/\\d+"); // d = digit ( 0 - 9 ), + = one or more
    }


}
