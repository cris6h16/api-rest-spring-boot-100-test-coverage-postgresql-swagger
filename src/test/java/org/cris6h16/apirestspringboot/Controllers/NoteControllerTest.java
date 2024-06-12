package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
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
import org.springframework.security.core.userdetails.UserDetails;
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
 * <p>
 * Here I load all the context of the application, if I don't make this and instead
 * when I only use {@link WebMvcTest} annotation, the spring security configuration
 * will be the default one, it means that any of your custom security config won't be applied...<br>
 * then if I want to use my custom security configuration for the tests I need to load the whole context...<br>
 * Here <strong>I WON'T LOAD THE WHOLE CONTEXT THEN MY CUSTOM SECURITY CONFIGURATION WILL BE LOST</strong>
 * (I only will use {@code @WebMvcTest}) because in the {@link NoteController}
 * I only have 1 security verification, and it's the {@code @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'USER')")} then
 * doesn't matter if I use the default security configuration, It just cares if I have a {@link Authentication} loaded
 * being an instance of {@link UserDetails} or any subclass of it.
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
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
     * Test the successful behavior of {@link NoteController#create(CreateNoteDTO, Long)}
     *
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
     * Test the successful behavior of {@link NoteController#getPage(Pageable, Long)}.
     *
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
     * Test the successful behavior of {@link NoteController#get(Long, Long)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
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
     * Test the successful behavior of {@link NoteController#update(Long, CreateNoteDTO, Long)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
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
     * Test the successful behavior of {@link NoteController#delete(Long, Long)}.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
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
