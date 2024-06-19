package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoteControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private NoteServiceImpl noteService;

    private static String path = Cons.Note.Controller.Path.PATH;

    @Test
    @Order(1)
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_successful_Then201_Created() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), anyLong())).thenReturn(222L);

        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches("/api/v1/notes/222");
        verify(noteService).create(
                argThat(dto -> dto.getTitle().equals("My First Note") && dto.getContent().equals("note of cris6h16")),
                eq(1L)
        );
    }

    @Test
    void create_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
        verify(noteService, never()).create(any(), any());
    }


    @Test
    @WithMockUserWithId(roles = {"ROLE_INVITED"})
    void create_hasRoleInvited_Then404_NotFound() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
        verify(noteService, never()).create(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_contentTypeNotSpecified_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
        verify(noteService, never()).create(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_contentTypeUnsupported_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("title=My First Note&content=note of cris6h16"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
        verify(noteService, never()).create(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_contentEmpty_then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.REQUEST_BODY_MISSING));
        verify(noteService, never()).create(any(), any());
    }

    /*
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
                Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG,
                Cons.User.Validations.USERNAME_IS_BLANK_MSG,
                Cons.User.Validations.EMAIL_IS_BLANK_MSG
        );
        verify(userService, never()).create(any(CreateUserDTO.class));
    }
     */
}