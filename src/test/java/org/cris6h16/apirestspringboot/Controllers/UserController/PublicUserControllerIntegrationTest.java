package org.cris6h16.apirestspringboot.Controllers.UserController;

import org.assertj.core.api.Assertions;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublicUserControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @Order(1)
    void create_contentTypeNotSpecified_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .content("{\"username\":\"cris6h16\",\"password\":\"12345678\", \"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(2)
    void create_contentTypeUnsupported_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=cris6h16&password=12345678"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
    }

    @Test
    @Order(3)
    void create_contentEmpty_then400_BAD_REQUEST() throws Exception {

        this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.REQUEST_BODY_MISSING));
    }

    @Test
    @Order(3)
    void create_givenInvalidJsonAttributes_Then400_BAD_REQUEST() throws Exception {
        String msg = this.mvc.perform(post(Cons.User.Controller.PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parameter\":\"cris6h16\",\"nonexistent\":\"12345678\", \"cris6h16\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andReturn().getResponse().getContentAsString();

        Assertions.assertThat(msg).containsAnyOf(
                Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG,
                Cons.User.Validations.USERNAME_IS_BLANK_MSG,
                Cons.User.Validations.EMAIL_IS_BLANK_MSG
        );
    }
}
