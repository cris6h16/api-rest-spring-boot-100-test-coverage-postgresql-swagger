package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.awt.print.Pageable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest// charge the context due my use of `@MyId` which use behind: `authentication.name.equalsIgnoreCase('anonymousUser') ? -1 : authentication.principal.id`
@AutoConfigureMockMvc
class UserControllerTest {


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
    void UserControllerTest_create_Successful_Then201AndReturnLocation() throws Exception {
        when(userService.create(any(CreateUpdateUserDTO.class))).thenReturn(1L);

        String location = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "cris6h16",
                                    "password": "12345678",
                                    "email": "cristianmherrera21@gmail.com"                                    
                                }
                                """)
                        .with(csrf())) // todo: test security like csrf
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
//    @WithMockUser
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void UserControllerTest_get_Successful_Then200AndReturnPublicUserDTOInJson() throws Exception {
        RoleDTO roleDTO = RoleDTO.builder()
                .name(user.getRoles().iterator().next().getName())
                .build();
        PublicUserDTO publicUserDTO = PublicUserDTO.builder()
                .id(user.getId()) // 1
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .roles((Set.of(roleDTO))) // immutable
                .build();
        when(userService.get(user.getId())).thenReturn(publicUserDTO);

        mvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0].name").value(ERole.ROLE_USER.name()));
    }


    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void UserControllerTest_update_Successful_Then204NoContent() throws Exception {
        doNothing().when(userService).update(any(Long.class), any(CreateUpdateUserDTO.class));

        mvc.perform(patch("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "newUsername"
                                 }
                                """)) // any content
                .andExpect(status().isNoContent());
    }



    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void UserControllerTest_delete_Successful_Then204NoContent() throws Exception {
        doNothing().when(userService).delete(any(Long.class));

        mvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
//    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_ADMIN"})
    void UserControllerTest_getUsers_Successful_Then200OkAndList() throws Exception {
        List<PublicUserDTO> users = new ArrayList<>();
        users.add(PublicUserDTO.builder()
                .id(1L)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .createdAt(new Date())
                .roles(Set.of(RoleDTO.builder().name(ERole.ROLE_USER).build()))
                .build());
        when(userService.get(any(PageRequest.class))).thenReturn(users);

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(users.size()))
                .andExpect(jsonPath("$[0].id").value(users.get(0).getId()))
                .andExpect(jsonPath("$[0].username").value(users.get(0).getUsername()))
                .andExpect(jsonPath("$[0].email").value(users.get(0).getEmail()))
                .andExpect(jsonPath("$[0].roles").isArray())
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].roles[0].name").value(ERole.ROLE_USER.name()));
    }

}