package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.NoteController;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test the exceptions caught by the `Advice` class that are not tested in the others
 * integration test classes of the CONTROLLER with ADVICE,
 * these are general exceptions normally are regardless to our implementation then
 * are out of the scope on any place of the app. and are directly passed to the Advice.
 * <p>
 * Here I load the whole context, if I only use the annotation {@link WebMvcTest}
 * the custom security configuration will not be loaded, and the app in testing
 * will use the default configuration.
 * </p>
 * <p>
 * Due to the mentioned above, If I want to test e.g.:
 * <ul>
 *     <li> GET {@code /helloword/not-found} we won't obtain
 *     {@link NoResourceFoundException}, instead we'll obtain {@link HttpStatus#UNAUTHORIZED} ( default behavior )</li>
 *     <li> GET {@code /api/users/1} we won't obtain {@link AccessDeniedException}, instead we'll obtain {@link HttpStatus#UNAUTHORIZED} ( default behavior )</li>
 * </ul>
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera</a>
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)/* we won't use it, but it's necessary for load the context with H2 as DB, then run the tests isolated from the real database*/
@AutoConfigureMockMvc
public class OtherExceptionsCaughtByAdviceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;

    @Test
    @Tag("NoResourceFoundException")
    void OtherExceptionsCaughtByAdviceTest_NoResourceFoundException() throws Exception {
        mvc.perform(get("/helloword/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
    }

    @Test
    @Tag("AccessDeniedException")
    void OtherExceptionsCaughtByAdviceTest_RequiredBeAuthenticated() throws Exception {
        String pathUsers = UserController.path;
        String pathNotes = NoteController.path;

        mvc.perform(get(pathUsers + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.ACCESS_DENIED));

        mvc.perform(get(pathNotes + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.ACCESS_DENIED));
    }

    @Test
    @Tag("MethodArgumentTypeMismatchException")
    void OtherExceptionsCaughtByAdviceTest_MethodArgumentTypeMismatchException() throws Exception {
        mvc.perform(get("/api/users/string"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }

    @Test
    @Tag("Exception")
    @WithMockUserWithId(id = 1)
    void OtherExceptionsCaughtByAdviceTest_Exception() throws Exception {

        doThrow(new NullPointerException()).when(userService).delete(any());

        mvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));

    }

}
