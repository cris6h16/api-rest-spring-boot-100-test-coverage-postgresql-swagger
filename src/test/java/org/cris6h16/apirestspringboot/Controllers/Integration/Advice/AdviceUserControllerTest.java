package org.cris6h16.apirestspringboot.Controllers.Integration.Advice;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Controllers.UserController;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.controller.UserController.IsNotYourIdException;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
 * @implNote Here I load all the context of the application, this due that if I only use
 * the {@link WebMvcTest} annotation, the security configuration will be the <strong>DEFAULT</strong> one.
 * Then my custom security configuration will be lost (e.g. {@code hasRole("ROLE_USER")} will be lost ).
 * For solve this problem I load the context, then the security configuration will be our custom one.<br>
 * <
 * <h3> The ways to solve this problem are: </h3>
 * <li>1. Load the context, then the security configuration will be our custom one</li>
 * <li>2. Create an empty security context, with an instance of {@link User} ( can be extended instances ), the point here is not be the {@code AnonymousUser} you can achieve this with the well known <strong>MockUsers</strong></li>
 * <li>3. Can exist more solutions, but the mentioned are those that I know</li>
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
     * Test the {@link UserControllerTransversalException} raised on {@link UserController}
     * and handled by the {@link ExceptionHandlerControllers}.
     * <p>
     * Test: try to fetch a user's account that isn't mine then {@link UserController#get(Long, Long)}
     * will throw the {@link IsNotYourIdException} handled by the {@link ExceptionHandlerControllers}
     * </p>
     *
     * @implNote the mentioned method depends on the {@link MyId} annotation, that's the reason why I mock a User
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
     * Test the {@link UserControllerTransversalException}, this is the unique exception which will be thrown
     * from the {@link UserController}, this will be handled by the {@link ExceptionHandlerControllers}.
     *
     * <p>
     * Here I'm testing the {@link UserController#update(Long, CreateUpdateUserDTO, Long)} method. it verify
     * if {@code principal.id != id_TriedToUpdate} then {@link UserController} will throw the
     * {@link UserControllerTransversalException} if is {@code true}, in this case  is {@code true}
     * </p>
     *
     * @implNote in the used method has an annotated parameter with {@link MyId} to inject the {@code Principal.id}
     * to the method to verify if is its user ID. That's the reason why I use the {@link WithMockUserWithId}
     * annotation to mock the {@code Principal}.
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
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
     * Test the {@link UserControllerTransversalException}, this is the unique exception which will be thrown
     * from the {@link UserController}, this will be handled by the {@link ExceptionHandlerControllers}.
     *
     * <p>
     * Here I'm testing the {@link UserController#delete(Long, Long)} method. it verifies
     * if {@code principal.id != id_TriedToDelete} then {@link UserController} will throw the
     * {@link UserControllerTransversalException} if is {@code true}, in this case  is {@code true}
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
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
     * Test the {@link UserControllerTransversalException}, this is the unique exception which will be thrown
     * from the {@link UserController}, this will be handled by the {@link ExceptionHandlerControllers}.
     * <p>
     * Here I'm testing the call to {@link UserController#getUsers(Pageable)}  method as not Admin. it verifies if the user is not an admin,
     * if it isn't an admin then {@link UserController} will throw the {@link UserControllerTransversalException}
     * </p>
     *
     * @implNote here I mock a user with the required role, method annotated has the annotation:<br>
     * {@code  @PreAuthorize("hasRole(T(org.cris6h16.apirestspringboot.Entities.ERole).ROLE_ADMIN)")}
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