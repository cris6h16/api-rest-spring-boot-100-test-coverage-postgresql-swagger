package org.cris6h16.apirestspringboot.Controllers.UserController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminUserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Order(1)
    void getPage_successful_Then200_Ok() throws Exception {
        List<PublicUserDTO> mockedUsersList = createPublicUserDTOs(20);
        when(userService.get(any(Pageable.class)))
                .thenReturn(mockedUsersList);

        String body = this.mvc.perform(get(Cons.User.Controller.PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        List<PublicUserDTO> retrievedFromController = Arrays.asList(
                objectMapper.readValue(body, PublicUserDTO[].class)
        );

        assertThatCollection(retrievedFromController)
                .containsExactlyElementsOf(mockedUsersList);
    }


    @Test
    @WithMockUser
    @Order(1)
    void getPage_isNotAnAdmin_Then401_UNAUTHORIZED() throws Exception {
        this.mvc.perform(get(Cons.User.Controller.PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.UNAUTHORIZED));
    }

    private List<PublicUserDTO> createPublicUserDTOs(int i) {
        List<PublicUserDTO> l = new ArrayList<>();
        Set<PublicRoleDTO> roles = new HashSet<>(Set.of(PublicRoleDTO.builder().name(ERole.ROLE_USER).build()));

        for (int j = 0; j < i; j++) {
            l.add(PublicUserDTO.builder()
                    .id((long) j)
                    .username("cris6h16" + j)
                    .email("cris6h16" + j + "@gmail.com")
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .roles(roles)
                    .build());
        }
        return l;
    }
}