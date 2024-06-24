package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.net.URIBuilder;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Controller.Path.NOTE_PATH;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
class NoteControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteServiceImpl noteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    UserEntity userEntity;
    String noEncryptedPassword = "12345678";
    @Autowired
    private NoteServiceImpl noteServiceImpl;


    @BeforeEach
    void setUp() {
        userService.deleteAll();
        noteServiceImpl.deleteAll();

        Long id = userService.create(CreateUserDTO.builder()
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password(noEncryptedPassword)
                .build());
        assertThat(userRepository.existsById(id)).isTrue();
        userEntity = userRepository.findById(id).get();
    }


    // -------------------------------- CREATE --------------------------------\\

    @Test
    void create_successful_Then201_Created() throws Exception {
        CreateNoteDTO dto = CreateNoteDTO.builder()
                .title("My First Note")
                .content("note of cris6h16")
                .build();

        ResponseEntity<Void> response = this.restTemplate
                .withBasicAuth(userEntity.getUsername(), noEncryptedPassword)
                .postForEntity(NOTE_PATH, dto, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        String location = response.getHeaders().getLocation().toString();
        assertThat(location).matches(NOTE_PATH + "/\\d+"); // d = digit ( 0 - 9 ), + = one or more
        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        NoteEntity noteEntity = noteRepository.findById(id).orElse(null);
        assertThat(noteEntity)
                .isNotNull()
                .hasFieldOrPropertyWithValue("title", dto.getTitle())
                .hasFieldOrPropertyWithValue("content", dto.getContent());
        assertThat(noteEntity.getUpdatedAt()).isBeforeOrEqualTo(new Date());
    }
    // -------------------------------- GET PAGE --------------------------------\\


    @Test
    void getPage_successful_Then200_Ok() throws Exception {
        List<CreateNoteDTO> createNoteDTOList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            createNoteDTOList.add(CreateNoteDTO.builder()
                    .title("My First Note " + i)
                    .content("note of cris6h16 " + i)
                    .build());
        }

        createNoteDTOList.forEach(dto -> {
            Long id = noteServiceImpl.create(dto, userEntity.getId());
        });


        URI uri = UriComponentsBuilder.fromPath(NOTE_PATH)
                .queryParam("page", 0)
                .queryParam("size", 25)
                .queryParam("sort", "id,desc")
                .build().toUri();

        ResponseEntity<PublicNoteDTO[]> response = this.restTemplate
                .withBasicAuth(userEntity.getUsername(), noEncryptedPassword)
                .getForEntity(uri, PublicNoteDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(24);

        List<PublicNoteDTO> publicNoteDTOList = Arrays.stream(response.getBody()).toList();
        assertThat(publicNoteDTOList).isSortedAccordingTo(Comparator.comparing(PublicNoteDTO::getId).reversed());

        for (int i = 0; i < createNoteDTOList.size(); i++) {
            CreateNoteDTO createDto = createNoteDTOList.get(i);
            PublicNoteDTO publicDto = publicNoteDTOList.get(createNoteDTOList.size() - i - 1); // reversed

            assertThat(createDto.getTitle()).isEqualTo(publicDto.getTitle());
            assertThat(createDto.getContent()).isEqualTo(publicDto.getContent());
        }

    }
    @Test
//    @Order(2)
//    @WithMockUserWithId(id = 1, roles = {"ROLE_USER"})
//    void getPage_successful_Then200_Ok() throws Exception {
//        when(noteService.getPage(any(), anyLong()))
//                .thenReturn(create10FixedPublicNoteDTO());
//
//        String listStr = this.mvc.perform(get(path))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse().getContentAsString();
//
//        assertThat(this.objectMapper.writeValueAsString(create10FixedPublicNoteDTO()))
//                .isEqualTo(listStr);
//    }

    // -------------------------------- GET --------------------------------\\

//    @Test
//    @Order(3)
//    @WithMockUserWithId(id = 1L)
//    void getByIdAndUserId_successful_Then200_Ok() throws Exception {
//        when(noteService.getByIdAndUserId(any(Long.class), any(Long.class)))
//                .thenReturn(createFixedPublicUserDTO());
//
//        String dtoS = this.mvc.perform(get(path + "/10"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse().getContentAsString();
//
//        assertThat(objectMapper.readValue(dtoS, PublicNoteDTO.class))
//                .isEqualTo(createFixedPublicUserDTO());
//
//        verify(noteService, times(1)).getByIdAndUserId(10L, 1L);
//    }

    // -------------------------------- PUT --------------------------------\\


//    @Test
//    @Order(4)
//    @WithMockUserWithId(id = 1L)
//    void put_successful_Then204_NoContent() throws Exception {
//        doNothing()
//                .when(noteService)
//                .put(anyLong(), anyLong(), any(CreateNoteDTO.class));
//
//        this.mvc.perform(put(path + "/10")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"title\":\"My First Note\",\"content\":\"note of cris6h16\"}"))
//                .andExpect(status().isNoContent());
//
//        verify(noteService, times(1))
//                .put(
//                        eq(10L),
//                        eq(1L),
//                        argThat(dto -> dto.getTitle().equals("My First Note") && dto.getContent().equals("note of cris6h16"))
//                );
//    }

    // -------------------------------- DELETE --------------------------------\\

//    @Test
//    @Order(5)
//    @WithMockUserWithId(id = 1L)
//    void delete_successful_Then204_NoContent() throws Exception {
//        doNothing()
//                .when(noteService)
//                .delete(anyLong(), anyLong());
//
//        this.mvc.perform(delete(path + "/10")
//                        .with(csrf()))
//                .andExpect(status().isNoContent());
//
//        verify(noteService, times(1))
//                .delete(10L, 1L);
//    }
}