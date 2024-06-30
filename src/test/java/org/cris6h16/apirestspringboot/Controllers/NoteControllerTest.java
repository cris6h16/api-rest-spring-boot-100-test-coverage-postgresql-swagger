package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.cris6h16.apirestspringboot.Services.NoteServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link NoteController},
 * tested also the security configuration for the note endpoints
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("UnitTest") // @WebMvcTest doesn't work with spring security custom configuration
class NoteControllerTest {
    @Autowired
    private MockMvc mvc;


    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteServiceImpl noteService;

    private static String path = Cons.Note.Controller.Path.NOTE_PATH;

    @AfterEach
    void tearDown() {
        clearInvocations(noteService);
        reset(noteService);
    }


    // -------------------------------- CREATE --------------------------------\\

    @Test
    @Order(1)
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_successful_Then201_Created() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), anyLong())).thenReturn(222L);

        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches("/api/v1/notes/222");
        verify(noteService).create(
                argThat(dto -> dto.getTitle().equals("My First Note") && dto.getContent().equals("note of cris6h16")),
                eq(1L)
        );
    }

    @Test
    void create_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).create(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_contentTypeNotSpecified_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).create(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_contentTypeUnsupported_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("title=My First Note&content=note of cris6h16"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).create(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_contentEmpty_then403_FORBIDDEN() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).create(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_givenInvalidJsonAttributes_DTO_Then400_BAD_REQUEST() throws Exception {
        String msg = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hello\":\"My first note\",\"word\":\"owned by cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(msg).containsAnyOf(
                Cons.Note.Validations.CONTENT_IS_NULL_MSG,
                Cons.Note.Validations.TITLE_IS_BLANK_MSG
        );
        verify(noteService, never()).create(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_titleNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"owned by cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Note.Validations.TITLE_IS_BLANK_MSG));
        verify(noteService, never()).create(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_givenEmptyTitle_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"owned by cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Note.Validations.TITLE_IS_BLANK_MSG));
        verify(noteService, never()).create(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_contentNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Note.Validations.CONTENT_IS_NULL_MSG));
        verify(noteService, never()).create(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 100, roles = {"ROLE_USER"})
    void create_givenEmptyContent_DTO_Then201_Created() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), anyLong()))
                .thenReturn(222L);

        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Note of cris6h16\",\"content\":\"\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches(path + "/222");
        verify(noteService, times(1)).create(
                argThat(dto -> dto.getTitle().equals("Note of cris6h16") && dto.getContent().isEmpty()),
                eq(100L));
    }

    @Test
    @WithMockUserWithId(id = 100, roles = {"ROLE_USER"})
    void create_givenJsonAttributesUntrimmed_Then_201_Created() throws Exception {// depends on service
        when(noteService.create(any(CreateNoteDTO.class), anyLong()))
                .thenReturn(222L);

        String location = this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"    Note of cris6h16 \",\"content\":\" my content    \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).matches(path + "/222");
        verify(noteService, times(1)).create(
                argThat(dto -> dto.getTitle().trim().equals("Note of cris6h16") && dto.getContent().trim().equals("my content")),
                eq(100L));
    }

    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), anyLong()))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_ADMIN"})
    void create_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringInBody() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), anyLong()))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: Unhandled Exception cris6h16's"));
    }


    @Test
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void create_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(noteService.create(any(CreateNoteDTO.class), anyLong()))
                .thenThrow(new ProperExceptionForTheUser(HttpStatus.HTTP_VERSION_NOT_SUPPORTED, "cris6h16's  handleable exception"));

        this.mvc.perform(post(path)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isHttpVersionNotSupported())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's  handleable exception"));
    }

    // -------------------------------- GET PAGE --------------------------------\\

    @Test
    @Order(2)
    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
    void getPage_successful_Then200_Ok() throws Exception {
        when(noteService.getPage(any(), anyLong()))
                .thenReturn(create10FixedPublicNoteDTO());

        String listStr = this.mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(this.objectMapper.writeValueAsString(create10FixedPublicNoteDTO()))
                .isEqualTo(listStr);
    }

    @Test
    void getPage_isNotAuthenticated_Then401_UNAUTHORIZED() throws Exception {
        this.mvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).getPage(any(), anyLong());
    }


    @Test
    @WithMockUserWithId(id = 100L)
    void getPage_PageableDefaultParamsWork() throws Exception {
        when(noteService.getPage(any(Pageable.class), anyLong()))
                .thenReturn(List.of());

        this.mvc.perform(get(path)).andExpect(status().isOk());

        verify(noteService).getPage(
                argThat(pageable ->
                        pageable.getPageNumber() == Cons.Note.Page.DEFAULT_PAGE &&
                                pageable.getPageSize() == Cons.Note.Page.DEFAULT_SIZE &&
                                pageable.getSort().getOrderFor(Cons.Note.Page.DEFAULT_SORT).getDirection().equals(Sort.Direction.ASC)
                ),
                eq(100L)
        );
    }


    @Test
    @WithMockUserWithId(id = 101L)
    void getPage_PageableCustomParamsWork() throws Exception {
        when(noteService.getPage(any(Pageable.class), anyLong()))
                .thenReturn(List.of());

        this.mvc.perform(get(path + "?page=7&size=21&sort=cris6h16,desc"))
                .andExpect(status().isOk());

        verify(noteService).getPage(
                argThat(pageable ->
                        pageable.getPageNumber() == 7 &&
                                pageable.getPageSize() == 21 &&
                                pageable.getSort().getOrderFor("cris6h16").getDirection().equals(Sort.Direction.DESC)
                ),
                eq(101L)
        );
    }

    @Test
    @WithMockUserWithId
    void getPage_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(noteService.getPage(any(Pageable.class), anyLong()))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(get(path))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(roles = {"ROLE_ADMIN"})
    void getPage_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringInBody() throws Exception {
        when(noteService.getPage(any(Pageable.class), anyLong()))
                .thenThrow(new NullPointerException("123 Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(get(path))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: 123 Unhandled Exception cris6h16's"));
    }

    @Test
    @WithMockUserWithId
    void getPage_HandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(noteService.getPage(any(Pageable.class), anyLong()))
                .thenThrow(new ProperExceptionForTheUser(HttpStatus.TOO_EARLY, "cris6h16's handleable exception"));

        this.mvc.perform(get(path))
                .andExpect(status().isTooEarly())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's handleable exception"));
    }

    private List<PublicNoteDTO> create10FixedPublicNoteDTO() {
        List<PublicNoteDTO> notes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            notes.add(PublicNoteDTO.builder()
                    .id((long) i)
                    .title("Note " + i)
                    .content("Content of Note " + i)
                    .updatedAt(new Date(1718843698151L))
                    .build());
        }
        return notes;
    }


    // -------------------------------- GET --------------------------------\\

    @Test
    @Order(3)
    @WithMockUserWithId(id = 1L)
    void getByIdAndUserId_successful_Then200_Ok() throws Exception {
        when(noteService.getByIdAndUserId(any(Long.class), any(Long.class)))
                .thenReturn(createFixedPublicUserDTO());

        String dtoS = this.mvc.perform(get(path + "/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readValue(dtoS, PublicNoteDTO.class))
                .isEqualTo(createFixedPublicUserDTO());

        verify(noteService, times(1)).getByIdAndUserId(10L, 1L);
    }

    @Test
    void getByIdAndUserId_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).getByIdAndUserId(any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void getByIdAndUserId_InvalidIdIsAStr_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(get(path + "/one"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).getByIdAndUserId(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void getByIdAndUserId_requiredIdNotPassed_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(get(path + "/"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).getByIdAndUserId(any(), any());
    }


    @Test
    @WithMockUserWithId
    void getByIdAndUserId_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(noteService.getByIdAndUserId(anyLong(), anyLong()))
                .thenThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(roles = {"ROLE_ADMIN"})
    void getByIdAndUserId_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringInBody() throws Exception {
        when(noteService.getByIdAndUserId(anyLong(), anyLong()))
                .thenThrow(new NullPointerException("1234 Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES));

        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: 1234 Unhandled Exception cris6h16's"));
    }


    @Test
    @WithMockUserWithId
    void getByIdAndUserId_handledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        when(noteService.getByIdAndUserId(anyLong(), anyLong()))
                .thenThrow(new ProperExceptionForTheUser(HttpStatus.HTTP_VERSION_NOT_SUPPORTED, "cris6h16's message of my handled exception"));

        this.mvc.perform(get(path + "/1"))
                .andExpect(status().isHttpVersionNotSupported())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's message of my handled exception"));
    }


    private PublicNoteDTO createFixedPublicUserDTO() {
        return create10FixedPublicNoteDTO().get(0);
    }


    // -------------------------------- PUT --------------------------------\\


    @Test
    @Order(4)
    @WithMockUserWithId(id = 1L)
    void put_successful_Then204_NoContent() throws Exception {
        doNothing()
                .when(noteService)
                .put(anyLong(), anyLong(), any(CreateNoteDTO.class));

        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isNoContent());

        verify(noteService, times(1))
                .put(
                        eq(10L),
                        eq(1L),
                        argThat(dto -> dto.getTitle().equals("My First Note") && dto.getContent().equals("note of cris6h16"))
                );
    }


    @Test
    @WithMockUserWithId(id = 1L)
    void put_givenEmptyContent_DTO_Then204_NoContent() throws Exception {
        doNothing()
                .when(noteService)
                .put(anyLong(), anyLong(), any(CreateNoteDTO.class));

        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Note of cris6h16\",\"content\":\"\"}"))
                .andExpect(status().isNoContent());
        verify(noteService, times(1))
                .put(eq(10L), eq(1L), argThat(dto -> dto.getTitle().equals("Note of cris6h16") && dto.getContent().isEmpty()));
    }

    @Test
    @WithMockUserWithId
    void put_untrimmedJsonAttributes_Then204_NoContent() throws Exception {
        doNothing()
                .when(noteService)
                .put(anyLong(), anyLong(), any(CreateNoteDTO.class));

        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"    Note of cris6h16 \",\"content\":\" my content    \"}"))
                .andExpect(status().isNoContent());
        verify(noteService, times(1)).put(
                eq(10L), eq(1L), any()
        );
    }

    @Test
    void put_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(put(path + "/1").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).put(any(), any(), any());
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void put_InvalidIdIsAStr_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(get(path + "/one"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void put_requiredIdNotPassed_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(put(path + "/"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId
    void put_contentTypeNotSpecified_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(put(path + "/1")
                        .with(csrf())
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId
    void put_contentTypeUnsupported_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(put(path + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("title=My First Note&content=note of cris6h16"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId
    void put_contentEmpty_then403_FORBIDDEN() throws Exception {
        this.mvc.perform(put(path + "/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId
    void put_givenInvalidJsonAttributes_DTO_Then400_BAD_REQUEST() throws Exception {
        String msg = this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hello\":\"My first note\",\"word\":\"owned by cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(msg).containsAnyOf(
                Cons.Note.Validations.CONTENT_IS_NULL_MSG,
                Cons.Note.Validations.TITLE_IS_BLANK_MSG
        );
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId
    void put_titleNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"owned by cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Note.Validations.TITLE_IS_BLANK_MSG));
        verify(noteService, never()).put(any(), any(), any());
    }


    @Test
    @WithMockUserWithId
    void put_givenEmptyTitle_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"owned by cris6h16\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Note.Validations.TITLE_IS_BLANK_MSG));
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId
    void put_contentNotPassed_DTO_Then400_BAD_REQUEST() throws Exception {
        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Note.Validations.CONTENT_IS_NULL_MSG));
        verify(noteService, never()).put(any(), any(), any());
    }

    @Test
    @WithMockUserWithId
    void put_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(noteService)
                .put(anyLong(), anyLong(), any(CreateNoteDTO.class));

        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(roles = {"ROLE_ADMIN"})
    void put_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringInBody() throws Exception {
        doThrow(new NullPointerException("2024 Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(noteService)
                .put(anyLong(), anyLong(), any(CreateNoteDTO.class));

        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: 2024 Unhandled Exception cris6h16's"));
    }

    @Test
    @WithMockUserWithId
    void put_HandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ProperExceptionForTheUser(HttpStatus.TOO_EARLY, "cris6h16's handleable exception"))
                .when(noteService)
                .put(anyLong(), anyLong(), any(CreateNoteDTO.class));

        this.mvc.perform(put(path + "/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
                .andExpect(status().isTooEarly())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's handleable exception"));
    }

    // -------------------------------- DELETE --------------------------------\\

    @Test
    @Order(5)
    @WithMockUserWithId(id = 1L)
    void delete_successful_Then204_NoContent() throws Exception {
        doNothing()
                .when(noteService)
                .delete(anyLong(), anyLong());

        this.mvc.perform(delete(path + "/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(noteService, times(1))
                .delete(10L, 1L);
    }

    @Test
    void delete_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(delete(path + "/1").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).delete(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void delete_InvalidIdIsAStr_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(get(path + "/one"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).delete(any(), any());
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void delete_requiredIdNotPassed_Then403_FORBIDDEN() throws Exception {
        this.mvc.perform(delete(path + "/"))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
        verify(noteService, never()).delete(any(), any());
    }

    @Test
    @WithMockUserWithId
    void delete_UnhandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new NullPointerException("Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(noteService)
                .delete(anyLong(), anyLong());

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    @WithMockUserWithId(roles = {"ROLE_ADMIN"})
    void delete_UnhandledExceptionRaisedInServiceAsAdmin_ThenExceptionToStringInBody() throws Exception {
        doThrow(new NullPointerException("2024 Unhandled Exception " + Cons.TESTING.UNHANDLED_EXCEPTION_MSG_FOR_TESTING_PURPOSES))
                .when(noteService)
                .delete(anyLong(), anyLong());

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("java.lang.NullPointerException: 2024 Unhandled Exception cris6h16's"));
    }

    @Test
    @WithMockUserWithId
    void delete_HandledExceptionRaisedInService_PassedToAdviceSuccessfully() throws Exception {
        doThrow(new ProperExceptionForTheUser(HttpStatus.TOO_EARLY, "cris6h16's handleable exception"))
                .when(noteService)
                .delete(anyLong(), anyLong());

        this.mvc.perform(delete(path + "/1")
                        .with(csrf()))
                .andExpect(status().isTooEarly())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("cris6h16's handleable exception"));
    }
}