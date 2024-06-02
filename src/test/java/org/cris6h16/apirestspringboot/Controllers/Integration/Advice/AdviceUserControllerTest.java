package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdviceUserControllerTest {


    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;

    UserEntity user;

    @BeforeEach
    void setUp() {
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(RoleEntity.builder()
                .id(1L)
                .name(ERole.ROLE_USER)
                .build());

        user = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("12345678")
                .roles(roles)
                .build();

    }

    @Test
    void AdviceUserControllerTest_create_fromService_AbstractExceptionWithStatus() throws Exception {
        when(userService.create(any(CreateUpdateUserDTO.class)))
                .thenThrow(new UserServiceTransversalException(Cons.User.DTO.NULL, HttpStatus.BAD_REQUEST)); // handled random exception

        mvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"username\":\"cris6h16\"}")) // irrelevant
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.User.DTO.NULL));
    }

    @Test
    void AdviceUserControllerTest_RequiredBeAuthenticated() throws Exception {
        mvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUserWithId(id = 2, username = "cris6h16", roles = {"ROLE_HELLOWORD"}) // in HTTP GET I'm using: "isAuthenticated()"
    void AdviceUserControllerTest_get_OtherUserAccount_Then403AndFailMsg() throws Exception {
        mvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG));
    }


    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void AdviceUserControllerTest_update_OtherUserAccount_Then403AndFailMsg() throws Exception {
        mvc.perform(patch("/api/users/2") // id is diff
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "newUsername"
                                 }
                                """)) // any content
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG));
    }

    @Test  // todo: explain why in all isn't necessary the mocking(verification is first)
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void AdviceUserControllerTest_delete_OtherUserAccount_Then403AndFailMsg() throws Exception {
        mvc.perform(delete("/api/users/2")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG));
    }

    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void AdviceUserControllerTest_getUsers_IsNotAdmin_Then403() throws Exception {
        mvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

}