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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OtherExceptionsCaughtByAdviceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;
    // todo: shutdown postgresql server after run the tests to ensure independence

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

    /**
     * This test is impossible to pass because the `objectMapper` is an internal class.
     * If we mock it, the app is going to crash, but the behavior is very predictable,
     * the method untested is the following ( it can provoke that the test coverage doesn't reach to 100% :'\ ):
     *
     * <pre>
     * {@code
     * String getMapInJson(Map map) {
     *     try {
     *         return objectMapper.writeValueAsString(map);
     *     } catch (Exception e) {
     *         log.error("ERROR PARSING TO JSON: {}", e.getMessage());
     *         return "ERROR PARSING TO JSON";
     *     }
     * }}
     * </pre>
     *
     * @throws Exception
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera</a>
     */

    @Test
    @Tag("Exception")
    @WithMockUserWithId(id = 1)
    @Disabled("IMPOSSIBLE test")
    void OtherExceptionsCaughtByAdviceTest_FailParsingToJson() throws Exception {

            doThrow(new NullPointerException()).when(objectMapper).writeValueAsString(any());

            mvc.perform(delete("/api/users/1")
                            .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));

        }


    }
