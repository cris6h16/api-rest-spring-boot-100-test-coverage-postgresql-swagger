package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicRoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * test class for {@link UserController}, here I test the endpoints of the controller
 * <b>just</b> when the operation is <strong>successful</strong>.<br>
 * This due that any exception in the controller layer or any layer below
 * will be handled by the {@link ExceptionHandlerControllers} which should be tested
 * as integration test with the controller layer.<br>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
 * @implNote Here I load all the context of the application, just for avoid the failures
 * on the {@link UserController#create(CreateUpdateUserDTO)} which isn't necessary be authenticated
 * for invoke it (in my security configuration is {@code permitAll()});<br>
 * when we only use {@link WebMvcTest} annotation, the spring security configuration
 * will be the default one, it means that any request will be unauthorized(default behavior)...<br>
 * then if we're an {@code AnonymousUser}( unauthenticated principal default ) any request will be unauthorized...<br>
 * Ok, ok, ok, ok, but the solution ?
 * <ul>
 *     <li>1. Load the context, then the security configuration will be our custom one ( enough if our method is {@code permitAll()} )</li>
 *     <li>2. Load the context && Create an empty security context, with an instance of {@link UserWithId} ( extended class of {@link User} ), achieved by <strong>MockingUsers</strong></li>
 *     <li>3. Can exist more solutions, but the mentioned are those that I know</li>
 * </ul>
 * @see NoteController
 * @see ExceptionHandlerControllers
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)/* we won't use it, but it's necessary for load the context with H2 as DB, then run the tests isolated from the real database*/
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;

    String path = UserController.path;

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


    /**
     * Test the successful behavior of {@link UserController#create(CreateUpdateUserDTO)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    void UserControllerTest_create_Successful_Then201AndReturnLocation() throws Exception {
        when(userService.create(any(CreateUpdateUserDTO.class))).thenReturn(1L);

        String location = mvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "cris6h16",
                                    "password": "12345678",
                                    "email": "cristianmherrera21@gmail.com"                                    
                                }
                                """)) // todo: test security like csrf
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");
    }

    /**
     * Test the successful behavior of {@link UserController#get(Long, Long)}
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
//    @WithMockUser
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void UserControllerTest_get_Successful_Then200AndReturnPublicUserDTOInJson() throws Exception {
        PublicRoleDTO roleDTO = PublicRoleDTO.builder()
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


    /**
     * Test the successful behavior of {@link UserController#update(Long, CreateUpdateUserDTO, Long)}.
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
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


    /**
     * Test the successful behavior of {@link UserController#delete(Long, Long)}
     *
     * @implNote I'm using {@link MyId} to inject the {@code id} of the {@code principal} in the controller method, that is why I'm using {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void UserControllerTest_delete_Successful_Then204NoContent() throws Exception {
        doNothing().when(userService).delete(any(Long.class));

        mvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Test the successful behavior of {@link UserController#getUsers(Pageable)},
     * here is tested adding an {@link Authentication} in an empty security context with a {@link UserWithId} ({@link WithMockUserWithId}),
     * this set user has the role: {@link  ERole#ROLE_ADMIN}.
     *
     * @implNote this tested method is <br>
     * {@code  @PreAuthorize("hasRole(T(org.cris6h16.apirestspringboot.Entities.ERole).ROLE_ADMIN)")}<br>
     * then I mock the principal with the role {@link ERole#ROLE_ADMIN}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_ADMIN"})
    void UserControllerTest_getUsers_Successful_Then200OkAndList() throws Exception {
        List<PublicUserDTO> users = new ArrayList<>();
        users.add(PublicUserDTO.builder()
                .id(1L)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .createdAt(new Date())
                .roles(Set.of(PublicRoleDTO.builder().name(ERole.ROLE_USER).build()))
                .build());
        when(userService.get(any(PageRequest.class))).thenReturn(users);

        mvc.perform(get(path))
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