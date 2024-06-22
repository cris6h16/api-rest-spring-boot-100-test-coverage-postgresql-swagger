package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.cris6h16.apirestspringboot.Utils.FilesSyncUtils;
import org.cris6h16.apirestspringboot.Utils.SychFor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExceptionHandlerControllersTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    @MockBean
    private FilesSyncUtils filesSyncUtils;

    @Test
    void handleConstraintViolationException_withViolations_Then400_BAD_REQUEST_andCustomMsg() throws Exception {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("My custom message in the validation");
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", Set.of(violation));

        when(userService.create(any(CreateUserDTO.class)))
                .thenThrow(exception);

        this.mvc.perform(post(Cons.User.Controller.Path.USER_PATH).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("My custom message in the validation"));
    }

    @Test
    void handleConstraintViolationException_withoutViolations_Then400_BAD_REQUEST_andGenericMsg() throws Exception {
        // Mock ConstraintViolationException without violations
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", new HashSet<>());

        when(userService.create(any(CreateUserDTO.class)))
                .thenThrow(exception);

        this.mvc.perform(post(Cons.User.Controller.Path.USER_PATH).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void handlePropertyReferenceException_withPropertyNonexistent_Then400_BAD_REQUEST_andSplittedMsg() throws Exception {
        PropertyReferenceException e = mock(PropertyReferenceException.class);
        when(e.getMessage()).thenReturn("No property 'passedProperty' found for type 'customEntity'");
        doThrow(e).when(userService).getPage(any(Pageable.class));

        this.mvc.perform(get(Cons.User.Controller.Path.USER_PATH))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No property 'passedProperty' found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void handlePropertyReferenceException_withUnformattedMessage_Then400_BAD_REQUEST_andGenericMsg() throws Exception {
        PropertyReferenceException e = mock(PropertyReferenceException.class);
        when(e.getMessage()).thenReturn("property passedProperty for sort was not found");
        doThrow(e).when(userService).getPage(any(Pageable.class));

        this.mvc.perform(get(Cons.User.Controller.Path.USER_PATH))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }

    /**
     * @see ExceptionHandlerControllers#handleDataIntegrityViolationException
     */
    @Test
    void handleDataIntegrityViolationException_withUsernameUniqueConstraint_Then409_CONFLICT_andCustomMsg() throws Exception {
        DataIntegrityViolationException e = mock(DataIntegrityViolationException.class);
        when(e.getMessage()).thenReturn("any string here" + Cons.User.Constrains.USERNAME_UNIQUE_NAME + "also here");

        when(userService.create(any(CreateUserDTO.class))).thenThrow(e);

        this.mvc.perform(post(Cons.User.Controller.Path.USER_PATH).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Constrains.USERNAME_UNIQUE_MSG));
    }

    /**
     * @see ExceptionHandlerControllers#handleDataIntegrityViolationException
     */
    @Test
    void handleDataIntegrityViolationException_withEmailUniqueConstraint_Then409_CONFLICT_andCustomMsg() throws Exception {
        DataIntegrityViolationException e = mock(DataIntegrityViolationException.class);
        when(e.getMessage()).thenReturn("any string here" + Cons.User.Constrains.EMAIL_UNIQUE_NAME + "also here");

        when(userService.create(any(CreateUserDTO.class))).thenThrow(e);

        this.mvc.perform(post(Cons.User.Controller.Path.USER_PATH).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Constrains.EMAIL_UNIQUE_MSG));
    }

    /**
     * @see ExceptionHandlerControllers#handleDataIntegrityViolationException
     */
    @Test
    void handleDataIntegrityViolationException_withUnhandledUniqueConstraint_Then409_CONFLICT_andGenericMsg() throws Exception {
        DataIntegrityViolationException e = mock(DataIntegrityViolationException.class);
        when(e.getMessage()).thenReturn("any string here" + "dni_unique" + "also here");

        when(userService.create(any(CreateUserDTO.class))).thenThrow(e);

        this.mvc.perform(post(Cons.User.Controller.Path.USER_PATH).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO())))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }

    // Required request body is missing
    @Test
    void handleHttpMessageNotReadableException_requiredBodyMissing_Then400_BAD_REQUEST_andCustomMsg() throws Exception {
        this.mvc.perform(post(Cons.User.Controller.Path.USER_PATH).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{}") --> validations fails in dto
                        .content("")) // doesn't create the dto
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.REQUEST_BODY_MISSING));
    }


    @Test
    void handleMethodArgumentNotValidException_ViolationsInDTO_Then400_BAD_REQUEST_andValidationMsg() throws Exception {
        this.mvc.perform(patch(Cons.User.Controller.Path.USER_PATH + Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PatchUsernameUserDTO.builder().build()))) // username blank
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));
    }


    @Test
    @WithMockUserWithId(id = 1)
    void handleMethodArgumentNotValidException_postInAPatchEndpoint_Then405_METHOD_NOT_ALLOWED_andGenericMsg() throws Exception {
        String path = Cons.User.Controller.Path.USER_PATH + Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME + "/1";
        this.mvc.perform(post(path).with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Request method 'POST' is not supported"));
    }

    @Test
    void handleMethodArgumentNotValidException_patchInAGetEndpoint_Then405_METHOD_NOT_ALLOWED_andGenericMsg() throws Exception {
        String path = Cons.User.Controller.Path.USER_PATH;
        this.mvc.perform(patch(path).with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Request method 'PATCH' is not supported"));
    }


    @Test
    void handleResponseStatusException_ThenStatusAndMsgFromTheException() throws Exception {
        when(userService.create(any(CreateUserDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "My custom message"));

        this.mvc.perform(post(Cons.User.Controller.Path.USER_PATH).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO())))
                .andExpect(status().isFailedDependency())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("My custom message"));
    }


    @Test
    void handleNoResourceFoundException_Unauthenticated_Then401_UNAUTHORIZED_andCustomMsg() throws Exception {
        this.mvc.perform(get("/helloword"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
    }

    @Test
    @WithMockUserWithId
    void handleNoResourceFoundException_Authenticated_Then404_NOT_FOUND_andCustomMsg() throws Exception {
        this.mvc.perform(get("/helloword"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
    }


    @Test
    void handleAccessDeniedException_Unauthenticated_Then401_UNAUTHORIZED_andCustomMsg() throws Exception {
        this.mvc.perform(get(Cons.User.Controller.Path.USER_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
    }

    @Test
    @WithMockUserWithId
    void handleAccessDeniedException_AuthenticatedUser_Then404_NOT_FOUND_andCustomMsg() throws Exception {
        this.mvc.perform(get(Cons.User.Controller.Path.USER_PATH))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
    }


    @Test
    void handleMethodArgumentTypeMismatchException_longPathVariableReceivedAStr_Then400_BAD_REQUEST_andGenricMsg() throws Exception {
        String path = Cons.User.Controller.Path.USER_PATH + Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME;
        this.mvc.perform(patch(path + "/one")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PatchUsernameUserDTO.builder().build()))) // username blank
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));

    }

    @Test
    void handleHttpMediaTypeNotSupportedException_Then415_UNSUPPORTED_MEDIA_TYPE_andCustomMsg() throws Exception {
        this.mvc.perform(patch(Cons.User.Controller.Path.USER_PATH + Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_XML)
                        .content(objectMapper.writeValueAsString(PatchUsernameUserDTO.builder().build())))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));
    }

    /**
     * @see ExceptionHandlerControllers#logHiddenExceptionForTheUser(Exception)
     */
    @Test
    @WithMockUserWithId(roles = "ROLE_ADMIN")
    void handleException_Then500_andCustomMsg_alsoShouldNotLogDueToExceptionMessageContainATestingPattern() throws Exception {
        when(userService.getPage(any(Pageable.class)))
                .thenThrow(new NullPointerException("Unexpected exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));
        this.mvc.perform(get(Cons.User.Controller.Path.USER_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
        verify(this.filesSyncUtils, never()).appendToFile(any(), any(), any());
    }

    /**
     * @see ExceptionHandlerControllers#logHiddenExceptionForTheUser(Exception)
     */
    @Test
    @WithMockUserWithId(roles = "ROLE_ADMIN")
    void handleException_Then500_andCustomMsg_alsoShouldLogDueToExceptionIsConsiderAsExceptionInProduction() throws Exception {
        when(userService.getPage(any(Pageable.class)))
                .thenThrow(new NullPointerException("Unexpected exception in production (doesnt contain the testing pattern)"));
        this.mvc.perform(get(Cons.User.Controller.Path.USER_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));

        verify(this.filesSyncUtils, times(1)).appendToFile(
                argThat(path -> path.toString().equals(Cons.Logs.HiddenExceptionsOfUsers)),
                argThat(line ->
                        line.toString().contains("Unexpected exception in production") &&
                                line.toString().contains("NullPointerException") &&
                                line.toString().split("::").length == 3
                ),
                eq(SychFor.HIDDEN_EXCEPTIONS_OF_USERS)
        );
    }


    private CreateUserDTO createUserDTO() {
        return CreateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build();
    }
}
