package org.cris6h16.apirestspringboot.Controllers.UserController;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("UnitTest") // @WebMvcTest doesn't work with spring security custom configuration
class PublicUserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;


    private static String path = Cons.User.Controller.Path.USER_PATH;


    @BeforeEach
    void setUp() {
        clearInvocations(userService);
        reset(userService);
    }

    @Test
    @Order(1)
    void create_successful_Then201_Created() throws Exception {
        when(userService.create(any(CreateUserDTO.class), any(ERole.class))).thenReturn(222L);

        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches("/api/v1/users/222");
        verify(userService).create(argThat(
                dto -> dto.getUsername().equals("cris6h16")
                        && dto.getPassword().equals("12345678")
                        && dto.getEmail().equals("cristianmherrera21@gmail.com")),
                eq(ERole.ROLE_USER)
                );
    }

    @Test
    void create_givenJsonAttributesUntrimmed_Then_201_Created() throws Exception {// depends on service
        when(userService.create(any(CreateUserDTO.class), any(ERole.class))).thenReturn(222L);

        String username = "  cris6h16 ";
        String password = " 12345678    ";
        String email = "        cristianmherrera21@gmail.com     ";
        String json = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\", \"email\":\"%s\"}",
                username,
                password,
                email
        );
        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches("/api/v1/users/222");
        verify(userService).create(any(CreateUserDTO.class), eq(ERole.ROLE_USER));
    }

    @Test
    @Order(2)
    void create_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(userService.create(any(CreateUserDTO.class), any(ERole.class)))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @Order(3)
    void create_HandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(userService.create(any(CreateUserDTO.class), any(ERole.class))) // random exception
                .thenThrow(new ProperExceptionForTheUser(HttpStatus.URI_TOO_LONG, "cris6h16's handleable exception"));

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUriTooLong())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's handleable exception"));
    }

    @Test
    @WithMockUserWithId(roles = {"ROLE_ADMIN"})
    void create_UnhandledExceptionRaisedInServiceAsAdmin_ThenFullExceptionInfo() throws Exception {
        when(userService.create(any(CreateUserDTO.class), any(ERole.class)))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: Unhandled Exception cris6h16's"));
    }

    @Test
    void create_contentTypeNotSpecified_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).create(any(CreateUserDTO.class));
    }

    @Test
    void create_contentTypeUnsupported_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=cris6h16&password=12345678"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).create(any(CreateUserDTO.class));
    }

    @Test
    void create_contentEmpty_then403_FORBIDDEN() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));

        verify(userService, never()).create(any());
    }

    @Test
    void create_givenInvalidJsonAttributes_DTO_Then400_BAD_REQUEST() throws Exception {
        String msg = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parameter\":\"cris6h16\",\"nonexistent\":\"12345678\", \"cris6h16\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(msg).containsAnyOf(
                Cons.User.Validations.USERNAME_IS_BLANK_MSG,
                Cons.User.Validations.EMAIL_IS_BLANK_MSG,
                Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG
        );

        verify(userService, never()).create(any(CreateUserDTO.class));
    }

    @Test
    void create_givenEmptyUsername_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));

        verify(userService, never()).create(any());
    }

    @Test
    void create_UsernameNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));

        verify(userService, never()).create(any());
    }

    @Test
    void create_givenEmptyEmail_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_IS_BLANK_MSG));

        verify(userService, never()).create(any());
    }

    @Test
    void create_EmailNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"12345678\", \"username\":\"cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_IS_BLANK_MSG));

        verify(userService, never()).create(any());
    }

    @Test
    void create_givenEmptyPassword_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\",\"password\":\"\", \"email\":\"cristianmherrera21@gmailc.om\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));
        verify(userService, never()).create(any());
    }

    @Test
    void create_PasswordNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\", \"email\":\"cristianmherrera21@gmailc.om\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));
        verify(userService, never()).create(any());
    }

}
