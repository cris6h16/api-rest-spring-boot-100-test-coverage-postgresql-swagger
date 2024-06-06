package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * test class for {@link NoteController}, here I test the endpoints of the controller
 * <b>just</b> when the operation is <strong>successful</strong>.<br>
 * This due that any exception in the controller layer or any layer below
 * will be handled by the {@link ExceptionHandlerControllers} which should be tested
 * as integration test with the controller layer.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
 * @see NoteController
 * @see ExceptionHandlerControllers
 * @since 1.0
 */
@WebMvcTest(NoteController.class)
public class NoteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NoteServiceImpl noteService;

    String path = NoteController.path;

    /**
     * Test the successful behavior of {@link NoteController#create(CreateNoteDTO, Long)},
     * here is tested adding an {@link Authentication} in an empty security context with a {@link UserWithId}
     ({@link WithMockUserWithId}).
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void NoteControllerTest_create_Successful_Then201AndReturnLocation() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), any(Long.class))).thenReturn(1L);

        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).isEqualTo(path + "/1");
    }


    /**
     * Test the successful behavior of {@link NoteController#getPage(Pageable, Long)},
     * here is tested adding an {@link Authentication} in an empty security context with a {@link UserWithId}
     ({@link WithMockUserWithId}).
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void NoteControllerTest_getPage_Successful_Then200AndReturnList() throws Exception {
        List<PublicNoteDTO> list = new ArrayList<>();
        list.add(PublicNoteDTO.builder().title("title").content("content").build());
        when(noteService.getPage(any(PageRequest.class), any(Long.class))).thenReturn(list);

        this.mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value(list.get(0).getTitle()))
                .andExpect(jsonPath("$[0].content").value(list.get(0).getContent()));
    }

    /**
     * Test the successful behavior of {@link NoteController#get(Long, Long)},
     * here is tested adding an {@link Authentication} in an empty security context with a {@link UserWithId}
     ({@link WithMockUserWithId}).
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @since 1.0
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void NoteControllerTest_get_Successful_Then200AndReturnNote() throws Exception {
        PublicNoteDTO note = PublicNoteDTO.builder().title("title").content("content").build();
        when(noteService.get(any(Long.class), any(Long.class))).thenReturn(note);

        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(note.getTitle()))
                .andExpect(jsonPath("$.content").value(note.getContent()));
    }

    /**
     * Test the successful behavior of {@link NoteController#update(Long, CreateNoteDTO, Long)},
     * here is tested adding an {@link Authentication} in an empty security context with a {@link UserWithId}
     ({@link WithMockUserWithId}).
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @since 1.0
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void NoteControllerTest_update_Successful_Then204() throws Exception {
        doNothing().when(noteService).put(any(Long.class), any(CreateNoteDTO.class), any(Long.class));

        this.mvc.perform(put(path + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isNoContent());
    }

    /**
     * Test the successful behavior of {@link NoteController#delete(Long, Long)},
     * here is tested adding an {@link Authentication} in an empty security context with a {@link UserWithId}
     ({@link WithMockUserWithId}).
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @since 1.0
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void NoteControllerTest_delete_Successful_Then204() throws Exception {
        doNothing().when(noteService).delete(any(Long.class), any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
