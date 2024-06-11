package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;


import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.NoteController;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    /**
     * Test: when is request an nonexistent endpoint then {@link NoResourceFoundException}
     * then response with {@link HttpStatus#NOT_FOUND} && {@link Cons.Response.ForClient#NO_RESOURCE_FOUND}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("NoResourceFoundException")
    void OtherExceptionsCaughtByAdviceTest_NoResourceFoundException() throws Exception {
        mvc.perform(get("/helloword/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
    }

    /**
     * Test: when is request with not enough privileges (Unauthenticated)
     * then {@link AccessDeniedException} then response with
     * {@link HttpStatus#FORBIDDEN} && {@link Cons.Auth.Fails#ACCESS_DENIED}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("AccessDeniedException")
    void OtherExceptionsCaughtByAdviceTest_RequiredBeAuthenticated() throws Exception {
        String pathUsers = UserController.path;
        String pathNotes = NoteController.path;

        mvc.perform(get(pathUsers + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));

        mvc.perform(get(pathNotes + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
    }

    /**
     * Test: unexpected type in path
     * (e.g. expected[ {@code GET `api/users/1`} ] but was[ {@code GET `api/users/hello`} ] )
     * then {@link MethodArgumentTypeMismatchException}
     * then response with {@link HttpStatus#BAD_REQUEST} && {@link Cons.Response.ForClient#GENERIC_ERROR}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("MethodArgumentTypeMismatchException")
    void OtherExceptionsCaughtByAdviceTest_MethodArgumentTypeMismatchException() throws Exception {
        mvc.perform(get("/api/users/string"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }

    /**
     * Unhandled Exception (unexpected), then simply generic error (all unexpected exceptions are logged
     * if we want make a specific response && code to this fail)
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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
