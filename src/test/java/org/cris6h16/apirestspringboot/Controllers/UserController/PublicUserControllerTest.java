package org.cris6h16.apirestspringboot.Controllers.UserController;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublicUserControllerTest { // test that and its integration with the service

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;

    @Test
    @Order(1)
    void create_successful_Then201_Created() throws Exception {
        when(userService.create(any(CreateUserDTO.class))).thenReturn(222L);

        String location = this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches("/api/v1/users/222");
    }

    //todo: doc why is created, all depends on the service
    @Test
    @Order(2)
    void create_givenJsonAttributesUntrimmed_Then_201_Created() throws Exception {// depends on service
        String username = "  cris6h16 ";
        String password = " 12345678    ";
        String email = "        cristianmherrera21@gmail.com     ";
        String json = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\", \"email\":\"%s\"}",
                username,
                password,
                email
        );

        when(userService.create(any(CreateUserDTO.class))).thenReturn(222L);

        String location = this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches("/api/v1/users/222");
    }

    @Test
    @Order(3)
    void create_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(userService.create(any(CreateUserDTO.class)))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }

    @Test
    @Order(4)
    void create_HandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(userService.create(any(CreateUserDTO.class))) // random exception
                .thenThrow(new ResponseStatusException(HttpStatus.URI_TOO_LONG, "cris6h16's handleable exception"));

        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUriTooLong())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's handleable exception"));
    }


    @Test
    void create_contentTypeNotSpecified_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    void create_contentTypeUnsupported_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=cris6h16&password=12345678"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    void create_contentEmpty_then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.REQUEST_BODY_MISSING));
    }

    @Test
    void create_givenInvalidJsonAttributes_DTO_Then400_BAD_REQUEST() throws Exception {
        String msg = this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parameter\":\"cris6h16\",\"nonexistent\":\"12345678\", \"cris6h16\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(msg).containsAnyOf(
                Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG,
                Cons.User.Validations.USERNAME_IS_BLANK_MSG,
                Cons.User.Validations.EMAIL_IS_BLANK_MSG
        );
    }

    @Test
    void create_givenEmptyUsername_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));
    }

    @Test
    void create_UsernameNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));
    }

    @Test
    @Disabled("test corresponding to service")
    void create_UsernameTooLong_DTO_Then400_BAD_REQUEST() throws Exception {
        String username = "a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1);

        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"?1\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}".replace("?1", username)))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_MAX_LENGTH_MSG));
    }


    @Test
    void create_givenEmptyEmail_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_IS_BLANK_MSG));
    }

    @Test
    void create_EmailNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"12345678\", \"username\":\"cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_IS_BLANK_MSG));
    }

    @Test
    @Disabled("test corresponding to entity")
    void create_InvalidEmail_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_INVALID_MSG));
    }


    @Test
    void create_givenEmptyPassword_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"\", \"email\":\"cristianmherrera21@gmailc.om\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));
    }

    @Test
    void create_PasswordNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\", \"email\":\"cristianmherrera21@gmailc.om\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));
    }

    @Test
    @Disabled("test corresponding to service")
    void create_Password7Chars_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"1234567\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));
    }


    // asser all was trimmed
}
