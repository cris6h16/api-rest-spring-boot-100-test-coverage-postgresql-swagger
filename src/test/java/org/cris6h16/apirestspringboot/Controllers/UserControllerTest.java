package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void UserControllerTest_create_Successful() throws Exception {
        String location = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "cris6h16",
                                    "password": "12345678",
                                    "email": "cristianmherrera21@gmail.com"                                    
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
//    @WithMockUser
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void UserControllerTest_get_Successful() throws Exception {
        mvc.perform(get("/api/users/4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void getUsers() {
    }

    @Test
    void verifyOwnership() {
    }
}