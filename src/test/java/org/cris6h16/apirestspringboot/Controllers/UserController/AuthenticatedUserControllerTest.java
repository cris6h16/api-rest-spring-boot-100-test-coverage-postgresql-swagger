package org.cris6h16.apirestspringboot.Controllers.UserController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticatedUserControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String path = Cons.User.Controller.Path.USER_PATH;
    private static final String path_patch_username = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME;
    private static final String path_patch_email = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_EMAIL;
    private static final String path_patch_password = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_PASSWORD;

    // -------------------------------------------------- GET --------------------------------------------------

    @Test
    @Order(1)
    @WithMockUserWithId(id = 1L)
    void getById_successful_Then200_Ok() throws Exception {
        when(userService.getById(any(Long.class)))
                .thenReturn(createFixedPublicUserDTO());

        String dtoS = this.mvc.perform(get(path + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readValue(dtoS, PublicUserDTO.class))
                .isEqualTo(createFixedPublicUserDTO());

        verify(userService, times(1)).getById(1L);
    }



    @Test
    @WithMockUserWithId(id = 1L)
    void getById_OtherUserAccount_Then403_Forbidden() throws Exception {
        this.mvc.perform(get(path + "/2"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Content-Type"))
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).getById(any(Long.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void getById_InvalidIdIsAStr_Then400_BadRequest() throws Exception {
        this.mvc.perform(get(path + "/one"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
        verify(userService, never()).getById(any(Long.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void getById_requiredIdNotPassed_Then404_NotFound() throws Exception {
        this.mvc.perform(get(path + "/"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
        verify(userService, never()).getById(any(Long.class));
    }

    @Test
    void getById_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
        verify(userService, never()).getById(any(Long.class));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_ADMIN"})
    void getById_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(userService.getById(any(Long.class)))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_ADMIN"})
    void getById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(userService.getById(any(Long.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.HTTP_VERSION_NOT_SUPPORTED, "cris6h16's message of my handled exception"));

        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isHttpVersionNotSupported())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's message of my handled exception"));
    }


    private PublicUserDTO createFixedPublicUserDTO() {
        Set<PublicRoleDTO> roles = new HashSet<>();
        roles.add(PublicRoleDTO.builder().name(ERole.ROLE_USER).build());

        return PublicUserDTO.builder()
                .id(1L)
                .username("cris6h16")
                .createdAt(new Date())
                .email("cristianmherrera21@gmail.com@gmail.com")
                .notes(new HashSet<>(0))
                .roles(roles)
                .createdAt(new Date(1718643989794L))
                .build();
    }


    // -------------------------------------------------- PATCH USERNAME --------------------------------------------------

    @Test
    @Order(3)
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_successful_Then204_NoContent() throws Exception {
        doNothing().when(userService).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));

        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\"}"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).patchUsernameById(eq(1L),
                argThat(dto -> dto.getUsername().equals("cris6h16")));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_InvalidIdIsAStr_Then400_BadRequest() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/one")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_requiredIdNotPassed_Then404_NotFound() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }

    @Test
    @WithMockUserWithId
    void patchUsernameById_contentTypeNotSpecified_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .content("{\"username\":\"cris6h16\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }


    @Test
    @WithMockUserWithId
    void patchUsernameById_contentTypeUnsupported_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=cris6h16"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }


    @Test
    @WithMockUserWithId
    void patchUsernameById_contentEmpty_then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.REQUEST_BODY_MISSING));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }

    @Test
    void patchUsernameById_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }



    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_OtherUserAccount_Then404_NotFound() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_givenInvalidJsonAttributes_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hello\":\"cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_givenEmptyUsername_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_UsernameNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.USERNAME_IS_BLANK_MSG));

        verify(userService, never()).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));

        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchUsernameById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.EARLY_HINTS, "cris6h16's message of my handled exception"))
                .when(userService).patchUsernameById(any(Long.class), any(PatchUsernameUserDTO.class));

        this.mvc.perform(patch(path_patch_username + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cris6h16\"}"))
                .andExpect(status().isEarlyHints())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's message of my handled exception"));
    }

    // -------------------------------------------------- PATCH EMAIL --------------------------------------------------\\

    @Test
    @Order(2)
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_successful_Then204_NoContent() throws Exception {
        doNothing().when(userService).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));

        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).patchEmailById(eq(1L),
                argThat(dto -> dto.getEmail().equals("cristianmherrera21@gmail.com")));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_InvalidIdIsAStr_Then400_BadRequest() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/one")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_requiredIdNotPassed_Then404_NotFound() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    @WithMockUserWithId
    void patchEmailById_contentTypeNotSpecified_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }


    @Test
    @WithMockUserWithId
    void patchEmailById_contentTypeUnsupported_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }


    @Test
    @WithMockUserWithId
    void patchEmailById_contentEmpty_then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.REQUEST_BODY_MISSING));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    void patchEmailById_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_OtherUserAccount_Then404_NotFound() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_givenInvalidJsonAttributes_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hello\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_IS_BLANK_MSG));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_givenEmptyEmail_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_IS_BLANK_MSG));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_EmailNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.EMAIL_IS_BLANK_MSG));

        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));

        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.EARLY_HINTS, "cris6h16's message of my handled exception"))
                .when(userService).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));

        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isEarlyHints())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's message of my handled exception"));
    }


    // -------------------------------------------------- PATCH PASSWORD --------------------------------------------------\\

    @Test
    @Order(3)
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_successful_Then204_NoContent() throws Exception {
        doNothing()
                .when(userService)
                .patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));

        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).patchPasswordById(eq(1L),
                argThat(dto -> dto.getPassword().equals("1234567")));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_InvalidIdIsAStr_Then400_BadRequest() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/one")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_requiredIdNotPassed_Then404_NotFound() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }

    @Test
    @WithMockUserWithId
    void patchPasswordById_contentTypeNotSpecified_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }


    @Test
    @WithMockUserWithId
    void patchPasswordById_contentTypeUnsupported_Then415_UNSUPPORTED_MEDIA_TYPE() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.UNSUPPORTED_MEDIA_TYPE));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }


    @Test
    @WithMockUserWithId
    void patchPasswordById_contentEmpty_then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.REQUEST_BODY_MISSING));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }

    @Test
    void patchPasswordById_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }



    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_OtherUserAccount_Then404_NotFound() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_givenInvalidJsonAttributes_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hello\":\"1234567\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_givenEmptyPassword_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_PasswordNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG));

        verify(userService, never()).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));

        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.EARLY_HINTS, "cris6h16's message of my handled exception"))
                .when(userService).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isEarlyHints())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's message of my handled exception"));
    }


    // -------------------------------------------------- DELETE --------------------------------------------------\\

    @Test
    @Order(4)
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_successful_Then204_NoContent() throws Exception {
        doNothing().when(userService).deleteById(any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteById(1L);
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_InvalidIdIsAStr_Then400_BadRequest() throws Exception {
        this.mvc.perform(delete(path + "/one")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
        verify(userService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_requiredIdNotPassed_Then404_NotFound() throws Exception {
        this.mvc.perform(delete(path + "/").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
        verify(userService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_Unauthenticated_Then404_NotFound() throws Exception {
        this.mvc.perform(delete(path + "/").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
        verify(userService, never()).deleteById(anyLong());
    }




    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_OtherUserAccount_Then404_NotFound() throws Exception {
        this.mvc.perform(delete(path + "/").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
        verify(userService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).deleteById(any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.EARLY_HINTS, "cris6h16's message of my handled exception"))
                .when(userService).deleteById(any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isEarlyHints())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's message of my handled exception"));
    }

}