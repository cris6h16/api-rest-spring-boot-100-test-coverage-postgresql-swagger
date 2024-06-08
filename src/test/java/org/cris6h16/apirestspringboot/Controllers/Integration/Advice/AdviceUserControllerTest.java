package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.AbstractExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.controller.UserControllerTransversalException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test the integration of the {@link UserController} with the {@link ExceptionHandlerControllers},
 * test the behavior of the handling of the extended classes of {@link AbstractExceptionWithStatus }
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
 * @implNote  Here I load all the context of the application, this due that if I only use
 * the {@link WebMvcTest} annotation, the security configuration will be the <strong>DEFAULT</strong> one.
 * Then my custom security configuration will be lost
 * (e.g. {@code @PreAuthorize('hasRole("ROLE_ADMIN")')} will be lost ).
 * For solve this problem I load the context, then the security configuration will be our custom one.<br>
 * <
 * <h3> The ways to solve this problem are: </h3>
 * <ul>
 *     <li>1. Load the context, then the security configuration will be our custom one ( enough if our method is {@code permitAll()} )</li>
 *     <li>2. Load the context && Create an empty security context, with an instance of {@link UserWithId} ( extended class of {@link User} ), achieved by <strong>MockingUsers</strong></li>
 *     <li>3. Can exist more solutions, but the mentioned are those that I know</li>
 * </ul>
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)  /* we won't use it, but it's necessary for load the context with H2 as DB, then run the tests isolated from the real database*/
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


    /**
     * Test the handling of {@link UserServiceTransversalException} raised on {@link UserServiceImpl}
     * to the {@link UserController}, and handled by the {@link ExceptionHandlerControllers}.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @implNote Used method: {@link UserController#create(CreateUpdateUserDTO)} is {@code permitAll()}.
     * @since 1.0
     */
    @Test
    void AdviceUserControllerTest_ExceptionFromService_AbstractExceptionWithStatus() throws Exception {
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


    /**
     * Test the handling of {@link UserControllerTransversalException}
     * raised on {@link UserController} and handled by the
     * {@link ExceptionHandlerControllers}.
     * <p>
     * Test: try to fetch a user's account that not correspond to the principal
     * ( based on {@code principal.id == id_triedToGet ? } )<br>
     * Method: {@link UserController#get(Long, Long)} is {@code isAuthenticated()}
     * </p>
     *
     * @implNote Endpoint method depends on {@link MyId} annotation due to that
     * I mock a user {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 2, username = "cris6h16", roles = {"ROLE_HELLOWORD"})
    void AdviceUserControllerTest_get_OtherUserAccount_Then403AndFailMsg() throws Exception {
        mvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG));
    }


    /**
     * Test the {@link UserControllerTransversalException} raised from
     * {@link UserController} then will be handled by the {@link ExceptionHandlerControllers}.
     *
     * <p>
     * Test: try to update a user's account that not correspond to the principal
     * ( based on {@code principal.id == id_triedToUpdate ? } )<br>
     * Method: {@link UserController#update(Long, CreateUpdateUserDTO, Long)} is {@code isAuthenticated()}
     * </p>
     *
     * @implNote Endpoint method depends on {@link MyId} annotation due to that I mock a user {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     **/
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

    /**
     * Test the {@link UserControllerTransversalException} raised from {@link UserController}
     * then will be handled by the {@link ExceptionHandlerControllers}.
     *
     * <p>
     * Test: try to delete a user's account that not correspond to the principal
     * ( based on {@code principal.id == id_triedToDelete ? } )<br>
     * Method: {@link UserController#delete(Long, Long)} is {@code isAuthenticated()}
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @implNote Endpoint method depends on {@link MyId} annotation due to that I mock a user {@link WithMockUserWithId}
     * @since 1.0
     **/

    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void AdviceUserControllerTest_delete_OtherUserAccount_Then403AndFailMsg() throws Exception {
        mvc.perform(delete("/api/users/2")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG));
    }

    /**
     * Test the {@link UserControllerTransversalException} raised from {@link UserController}
     * then will be handled by the {@link ExceptionHandlerControllers}.
     * <p>
     * Test: try to fetch all users, but the principal is not an admin<br>
     * Method: {@link UserController#getUsers(Pageable)} is {@code hasRole("ROLE_ADMIN")}
     * </p>
     *
     * @implNote I mock a user with the role {@code ROLE_USER} with the annotation {@link WithMockUserWithId}
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    @WithMockUserWithId(id = 1, username = "cris6h16", roles = {"ROLE_USER"})
    void AdviceUserControllerTest_getUsers_IsNotAdmin_Then403() throws Exception {
        mvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.ACCESS_DENIED));
    }
}