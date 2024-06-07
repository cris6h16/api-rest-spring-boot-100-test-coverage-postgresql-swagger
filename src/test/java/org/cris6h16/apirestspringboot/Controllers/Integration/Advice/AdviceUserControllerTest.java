package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Controllers.NoteController;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test the integration of the {@link UserController} with the {@link ExceptionHandlerControllers}
 * ( {@code Advice} ), here I wrote the test for the {@link UserServiceTransversalException}
 * which is the unique exception that can pass transversely through the layers.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
 * @implNote here I load the context due that in all methods on {@link NoteController} I inject the {@code  Principal.id } though the {@link MyId } annotation
 * @since 1.0
 */

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
 *     <li>1. Load the context, then the security configuration will be our custom one</li>
 *     <li>2. Create an empty security context, with an instance of {@link User} ( can be extended instances ), the point here is not be the {@code AnonymousUser} you can achieve this with the well known <strong>MockUsers</strong></li>
 *     <li>3. Can exist more solutions, but the mentioned are those that I know</li>
 * </ul>
 * @see NoteController
 * @see ExceptionHandlerControllers
 * @since 1.0
 */
//@SpringBootTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) //todo: doc my tip: kill the db for execute test to be sure that the test is isolated
//@AutoConfigureMockMvc
    @WebMvcTest(UserController.class)
/*
   todo: doc about why only using the `@WebMvcTest(UserController.class)` some tests will fail due to our custom security configuration will be lost then those will fail e.g.: hasRole("ROLE_USER") will be lost
 */
class AdviceUserControllerTest {


    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserServiceImpl userService;

    UserEntity user;
//    @Autowired
//    private UserRepository userRepository;

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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Cons.Auth.Fails.ACCESS_DENIED));
    }



}