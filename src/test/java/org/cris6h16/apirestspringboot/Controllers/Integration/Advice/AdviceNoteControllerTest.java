package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;

import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Controllers.NoteController;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test the integration of the {@link NoteController} with the {@link ExceptionHandlerControllers}.
 * here I wrote the test for the {@link NoteServiceTransversalException}
 * which is the unique exception that can pass transversely through the layers.
 * <p>
 * here I load the context due that in all methods on {@link NoteController}
 * I inject the {@code  Principal.id } though the {@link MyId } annotation
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) /* we won't use it, but it's necessary for load the context with H2 as DB, then run the tests isolated from the real database*/
public class AdviceNoteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NoteServiceImpl noteService;


    String path = NoteController.path;

    /**
     * we threw a {@link NoteServiceTransversalException} from {@link UserServiceImpl}
     * then passed through the {@link UserController} and finally handled in the {@link ExceptionHandlerControllers}
     * <p>
     * Random used Method: {@link NoteController#create(CreateNoteDTO, Long)}
     * this depends on {@link MyId} to inject {@code Principal.id}.
     * that the reason why I use the {@link WithMockUserWithId} annotation.
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void AdviceNoteControllerTest_ExceptionFromService_AbstractExceptionWithStatus() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), any(Long.class))).thenThrow(new NoteServiceTransversalException("fail msg", HttpStatus.UPGRADE_REQUIRED));

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isUpgradeRequired())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("fail msg"));
    }

}
