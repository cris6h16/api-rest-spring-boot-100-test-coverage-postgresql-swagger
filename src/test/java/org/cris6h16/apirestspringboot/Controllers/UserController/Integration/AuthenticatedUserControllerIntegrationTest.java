package org.cris6h16.apirestspringboot.Controllers.UserController.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
class AuthenticatedUserControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    Long id;
    CreateUserDTO dto;

    @MockBean
    private UserServiceImpl userService;

    private static final String path = Cons.User.Controller.Path.USER_PATH;
    private static final String path_patch_username = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME;
    private static final String path_patch_email = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_EMAIL;
    private static final String path_patch_password = path + Cons.User.Controller.Path.COMPLEMENT_PATCH_PASSWORD;


    @BeforeEach
    void setUp() {
        userService.deleteAll();
        dto = CreateUserDTO.builder()
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("12345678")
                .build();
        id = userService.create(dto);
    }

    // -------------------------------------------------- GET --------------------------------------------------

    @Test
    void getById_successful_Then200_Ok() throws Exception {

        ResponseEntity<PublicUserDTO> res = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .getForEntity(path + "/1", PublicUserDTO.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();

        PublicUserDTO pdto = res.getBody();
        assertThat(pdto)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("username", dto.getUsername())
                .hasFieldOrPropertyWithValue("email", dto.getEmail())
                .hasFieldOrPropertyWithValue("roles", Set.of(PublicRoleDTO.builder().name(ERole.ROLE_USER).build()))
                .hasFieldOrPropertyWithValue("notes", new HashSet<>(0))
                .hasNoNullFieldsOrPropertiesExcept("updatedAt");
        assertThat(pdto.getCreatedAt()).isInSameDayAs(new Date());
    }


    // -------------------------------------------------- PATCH USERNAME --------------------------------------------------

    @Test
    void patchUsernameById_successful_Then204_NoContent() throws Exception {
        PatchUsernameUserDTO dto = PatchUsernameUserDTO.builder().username("cris6h16").build();
        this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .exchange(path_patch_username + "/1", HttpMethod.PATCH, , Void.class);
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
    void patchEmailById_InvalidIdIsAStr_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/one")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchEmailById(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_requiredIdNotPassed_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchEmailById(any(), any());
    }

    @Test
    @WithMockUserWithId
    void patchEmailById_contentTypeNotSpecified_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchEmailById(any(), any());
    }


    @Test
    @WithMockUserWithId
    void patchEmailById_contentTypeUnsupported_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchEmailById(any(), any());
    }


    @Test
    @WithMockUserWithId
    void patchEmailById_contentEmpty_then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));
    }

    @Test
    void patchEmailById_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchEmailById(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_OtherUserAccount_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_email + "/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchEmailById(any(), any());
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
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_ADMIN"})
    void patchEmailById_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringToTheClient() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).patchEmailById(any(Long.class), any(PatchEmailUserDTO.class));

        this.mvc.perform(patch(path_patch_email + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cristianmherrera21@gmail.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: Unhandled Exception cris6h16's"));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchEmailById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ProperExceptionForTheUser(HttpStatus.EARLY_HINTS, "cris6h16's message of my handled exception"))
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
    void patchPasswordById_InvalidIdIsAStr_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/one")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchPasswordById(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_requiredIdNotPassed_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchPasswordById(any(), any());
    }

    @Test
    @WithMockUserWithId
    void patchPasswordById_contentTypeNotSpecified_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchPasswordById(any(), any());
    }


    @Test
    @WithMockUserWithId
    void patchPasswordById_contentTypeUnsupported_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchPasswordById(any(), any());
    }


    @Test
    @WithMockUserWithId
    void patchPasswordById_contentEmpty_then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchPasswordById(any(), any());
    }

    @Test
    void patchPasswordById_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchPasswordById(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_OtherUserAccount_Then403_Forbidden() throws Exception {
        this.mvc.perform(patch(path_patch_password + "/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).patchPasswordById(any(), any());
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
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_ADMIN"})
    void patchPasswordById_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringToTheClient() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).patchPasswordById(any(Long.class), any(PatchPasswordUserDTO.class));

        this.mvc.perform(patch(path_patch_password + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1234567\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: Unhandled Exception cris6h16's"));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void patchPasswordById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ProperExceptionForTheUser(HttpStatus.EARLY_HINTS, "cris6h16's message of my handled exception"))
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
    void deleteById_InvalidIdIsAStr_Then403_Forbidden() throws Exception {
        this.mvc.perform(delete(path + "/one")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_requiredIdNotPassed_Then403_Forbidden() throws Exception {
        this.mvc.perform(delete(path + "/").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(delete(path + "/").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).deleteById(anyLong());
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_OtherUserAccount_Then403_Forbidden() throws Exception {
        this.mvc.perform(delete(path + "/").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(userService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).deleteById(any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_ADMIN"})
    void deleteById_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringToTheClient() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(userService).deleteById(any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: Unhandled Exception cris6h16's"));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void deleteById_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ProperExceptionForTheUser(HttpStatus.EARLY_HINTS, "cris6h16's message of my handled exception"))
                .when(userService).deleteById(any(Long.class));

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isEarlyHints())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's message of my handled exception"));
    }

}