package org.cris6h16.apirestspringboot.Controllers.UserController.Integration;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
class AuthenticatedUserControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    Long id;
    CreateUserDTO dto;

    @Autowired
    private UserServiceImpl userService;

    private static final String path = Cons.User.Controller.Path.USER_PATH;
    private static final String path_patch_username = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME;
    private static final String path_patch_email = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_EMAIL;
    private static final String path_patch_password = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_PASSWORD;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {
        userService.deleteAll();
        dto = CreateUserDTO.builder()
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("12345678")
                .build();
        id = userService.create(dto);
        assertThat(userRepository.existsById(id)).isTrue();
    }

    // -------------------------------------------------- GET --------------------------------------------------

    @Test
    void getById_successful_Then200_Ok() throws Exception {

        ResponseEntity<PublicUserDTO> res = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .getForEntity(path + "/1", PublicUserDTO.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();

        PublicUserDTO pdto = res.getBody();
        assertThat(pdto)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("username", dto.getUsername())
                .hasFieldOrPropertyWithValue("email", dto.getEmail())
                .hasFieldOrPropertyWithValue("roles", Set.of(PublicRoleDTO.builder().name(ERole.ROLE_USER).build()))
                .hasFieldOrPropertyWithValue("notes", new HashSet<>(0))
                .hasNoNullFieldsOrPropertiesExcept("updatedAt");
        assertThat(pdto.getCreatedAt()).isInSameDayAs(new Date());
    }


    // -------------------------------------------------- PATCH USERNAME --------------------------------------------------

    @Test
    void patchUsernameById_successful_Then204_NoContent() throws Exception {
        UserEntity inDb = userRepository.findById(id).orElse(null);

        PatchUsernameUserDTO patchDTO = PatchUsernameUserDTO.builder().username("githubcomcris6h16").build();
        HttpEntity<PatchUsernameUserDTO> entity = new HttpEntity<>(patchDTO, new HttpHeaders());

        ResponseEntity<Void> res = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .exchange(path_patch_username + "/" + id, HttpMethod.PATCH, entity, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        UserEntity updated = userRepository.findById(id).orElse(null);
        assertThat(inDb).isNotNull();
        assertThat(updated)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", inDb.getId())
                .hasFieldOrPropertyWithValue("username", "githubcomcris6h16")
                .hasFieldOrPropertyWithValue("email", inDb.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", inDb.getCreatedAt())
                .hasFieldOrPropertyWithValue("roles", inDb.getRoles()); //eager

        //update at
        assertThat(inDb.getUpdatedAt()).isNull();
        assertThat(updated.getUpdatedAt()).isNotNull();

        //password
        assertThat(updated.getPassword()).isEqualTo(inDb.getPassword()); // comparing encrypted passwords
    }


    // -------------------------------------------------- PATCH EMAIL --------------------------------------------------\\


    @Test
    void patchEmailById_successful_Then204_NoContent() throws Exception {
        UserEntity inDb = userRepository.findById(id).orElse(null);

        PatchEmailUserDTO patchDTO = PatchEmailUserDTO.builder().email("hello" + "cristianmherrera21@gmail.com").build();
        HttpEntity<PatchEmailUserDTO> entity = new HttpEntity<>(patchDTO, new HttpHeaders());

        ResponseEntity<Void> res = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .exchange(path_patch_email + "/" + id, HttpMethod.PATCH, entity, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        UserEntity updated = userRepository.findById(id).orElse(null);
        assertThat(inDb).isNotNull();
        assertThat(updated)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", inDb.getId())
                .hasFieldOrPropertyWithValue("username", inDb.getUsername())
                .hasFieldOrPropertyWithValue("email", "hello" + "cristianmherrera21@gmail.com")
                .hasFieldOrPropertyWithValue("createdAt", inDb.getCreatedAt())
                .hasFieldOrPropertyWithValue("roles", inDb.getRoles()); //eager

        //update at
        assertThat(inDb.getUpdatedAt()).isNull();
        assertThat(updated.getUpdatedAt()).isNotNull();

        //password
        assertThat(updated.getPassword()).isEqualTo(inDb.getPassword()); // comparing encrypted passwords

    }
    // -------------------------------------------------- PATCH PASSWORD --------------------------------------------------\\

    @Test
    void patchPasswordById_successful_Then204_NoContent() throws Exception {
        UserEntity inDb = userRepository.findById(id).orElse(null);

        PatchPasswordUserDTO patchDTO = PatchPasswordUserDTO.builder().password("newPassword12345").build();
        HttpEntity<PatchPasswordUserDTO> entity = new HttpEntity<>(patchDTO, new HttpHeaders());

        ResponseEntity<Void> res = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .exchange(path_patch_password + "/" + id, HttpMethod.PATCH, entity, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        UserEntity updated = userRepository.findById(id).orElse(null);
        assertThat(inDb).isNotNull();
        assertThat(updated)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", inDb.getId())
                .hasFieldOrPropertyWithValue("username", inDb.getUsername())
                .hasFieldOrPropertyWithValue("email", inDb.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", inDb.getCreatedAt())
                .hasFieldOrPropertyWithValue("roles", inDb.getRoles()); //eager

        //update at
        assertThat(inDb.getUpdatedAt()).isNull();
        assertThat(updated.getUpdatedAt()).isNotNull();

        //password
        assertThat(updated.getPassword()).isNotEqualTo(inDb.getPassword()); // comparing encrypted passwords
        assertThat(passwordEncoder.matches("newPassword12345", updated.getPassword())).isTrue();
    }

    // -------------------------------------------------- DELETE --------------------------------------------------\\


    @Test
    void deleteById_successful_Then204_NoContent() throws Exception {
        ResponseEntity<Void> res = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .exchange(path + "/" + id, HttpMethod.DELETE, null, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.existsById(id)).isFalse();
        assertThat(userRepository.count()).isZero();
    }


}