package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

///** todo: correct docs and doc why there isn't unit tests for the controller
// * test class for {@link NoteController}, here I test the endpoints of the controller
// * <b>just</b> when the operation is <strong>successful</strong>.<br>
// * This due that any exception in the controller layer or any layer below
// * will be handled by the {@link ExceptionHandlerControllers} which should be tested
// * as integration test with the controller layer.
// * <p>
// * Here I load all the context of the application, if I don't make this and instead
// * when I only use {@link WebMvcTest} annotation, the spring security configuration
// * will be the default one, it means that any of your custom security config won't be applied...<br>
// * then if I want to use my custom security configuration for the tests I need to load the whole context...<br>
// * Here <strong>I WON'T LOAD THE WHOLE CONTEXT THEN MY CUSTOM SECURITY CONFIGURATION WILL BE LOST</strong>
// * (I only will use {@code @WebMvcTest}) because in the {@link NoteController}
// * I only have 1 security verification, and it's the {@code @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'USER')")} then
// * doesn't matter if I use the default security configuration, It just cares if I have a {@link Authentication} loaded
// * being an instance of {@link UserDetails} or any subclass of it.
// * </p>
// *
// * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
// * @since 1.0
// */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) /* we won't use it, but it's necessary for load the context with H2 as DB, then run the tests isolated from the real database*/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NoteServiceImpl noteService;

    String path = NoteController.path;

    // ------------------- FAILURES ( ADVICE ) ------------------- \\

    /**
     * we threw a {@link NoteServiceTransversalException} from {@link UserServiceImpl}
     * then passed through the {@link UserController} and finally handled
     * in the {@link ExceptionHandlerControllers} with the status && message defined by the exception
     * <p>
     * PD: this type of test is necessary test just once in any method of the controller
     * just to verify that the advice is working when an transversal exception is thrown in the service layer
     * in this case I decided to test it in the {@link NoteController#create(CreateNoteDTO, Long)} method
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @Tag("Advice")
    @Order(1)
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void ExceptionFromService_AdviceWorking() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), any(Long.class)))
                .thenThrow(new NoteServiceTransversalException("fail msg", HttpStatus.UPGRADE_REQUIRED)); // random status && message

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isUpgradeRequired())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("fail msg"));
    }


    /**
     * Test when {@link NoteController#create(CreateNoteDTO, Long)} is called
     * with content type empty, then the response should be 415
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void create_contentTypeNotSpecified_Then415() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), any(Long.class))).thenReturn(1L);

        this.mvc.perform(post(path)
                        .with(csrf())
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
    }

    /**
     * Test when {@link NoteController#create(CreateNoteDTO, Long)} is called
     * with content type unsupported, then the response should be 415
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void create_contentTypeUnsupported_Then415() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), any(Long.class))).thenReturn(1L);

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("title=title&content=content"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
    }

    //todo: write the remaining

    /**
     * Test the behavior of {@link NoteController#get(Long, Long)} when
     * the user is an invited user, then the response should be 404
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @Tag("Advice")
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_INVITED"})
    void delete_IsInvited_Then404() throws Exception {
        doNothing().when(noteService).delete(any(Long.class), any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
    }


    // ------------------- SUCCESSFUL ------------------- \\

    /**
     * Test the successful behavior of {@link NoteController#create(CreateNoteDTO, Long)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void create_Successful_Then201AndReturnLocation() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), any(Long.class))).thenReturn(1L);

        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(""))
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
    void getPage_Successful_Then200AndReturnList() throws Exception {
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
    void get_Successful_Then200AndReturnNote() throws Exception {
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
    void update_Successful_Then204() throws Exception {
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
    void delete_Successful_Then204() throws Exception {
        doNothing().when(noteService).delete(any(Long.class), any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }


}
