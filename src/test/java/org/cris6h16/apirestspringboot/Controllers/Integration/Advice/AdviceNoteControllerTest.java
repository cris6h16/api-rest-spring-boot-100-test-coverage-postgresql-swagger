package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;

import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.NoteController;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest// charge the context due my use of `@MyId` which use behind: `authentication.name.equalsIgnoreCase('anonymousUser') ? -1 : authentication.principal.id`
@AutoConfigureMockMvc
public class AdviceNoteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NoteServiceImpl noteService;

    String path = NoteController.path;

    @Test
    @WithMockUserWithId(id = 1L, username = "cris6h16", password = "12345678", roles = {"ROLE_USER"})
    void NoteControllerTest_ExceptionFromService_AbstractExceptionWithStatus() throws Exception {
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
