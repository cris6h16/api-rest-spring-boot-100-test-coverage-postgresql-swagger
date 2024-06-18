package org.cris6h16.apirestspringboot.Controllers.UserController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @Order(1)
    @WithMockUserWithId(id = 1L)
    void get_successful_Then200_Ok() throws Exception {
        when(userService.get(any(Long.class)))
                .thenReturn(createFixedPublicUserDTO(1L));

        String dtoS = this.mvc.perform(get(Cons.User.Controller.PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readValue(dtoS, PublicUserDTO.class))
                .isEqualTo(createFixedPublicUserDTO(1L));

        verify(userService, times(1)).get(1L);
    }

    @Test
    void get_Unauthenticated_Then401_Unauthorized() throws Exception {
        this.mvc.perform(get(Cons.User.Controller.PATH + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
        verify(userService, never()).get(any(Long.class));
    }


    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_INVITED"})
    void get_hasRoleInvited_Then404_NotFound() throws Exception {
        when(userService.get(any(Long.class)))
                .thenReturn(createFixedPublicUserDTO(1L));

        this.mvc.perform(get(Cons.User.Controller.PATH + "/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
                        verify(userService, never()).get(any(Long.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_INVITED"})
    void get_OtherUserAccount_Then404_NotFound() throws Exception {
        this.mvc.perform(get(Cons.User.Controller.PATH + "/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
        verify(userService, never()).get(any(Long.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void get_InvalidIdIsAStr_Then400_BadRequest() throws Exception {
        this.mvc.perform(get(Cons.User.Controller.PATH + "/one"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.GENERIC_ERROR));
        verify(userService, never()).get(any(Long.class));
    }

    @Test
    @WithMockUserWithId(id = 1L, roles = {"ROLE_USER"})
    void get_requiredIdNotPassed_Then404_NotFound() throws Exception {
        this.mvc.perform(get(Cons.User.Controller.PATH + "/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Cons.Response.ForClient.NO_RESOURCE_FOUND));
        verify(userService, never()).get(any(Long.class));
    }




    private PublicUserDTO createFixedPublicUserDTO(Long id) {
        Set<PublicRoleDTO> roles = new HashSet<>();
        roles.add(PublicRoleDTO.builder().name(ERole.ROLE_USER).build());

        return PublicUserDTO.builder()
                .id(id)
                .username("cris6h16")
                .createdAt(new Date())
                .email("cristianmherrera21@gmail.com@gmail.com")
                .notes(new HashSet<>(0))
                .roles(roles)
                .createdAt(new Date(1718643989794L))
                .build();
    }


}